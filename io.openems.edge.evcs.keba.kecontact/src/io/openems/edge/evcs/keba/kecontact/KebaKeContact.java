package io.openems.edge.evcs.keba.kecontact;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.modbusslave.ModbusType;
import io.openems.edge.evcs.api.Evcs;
import io.openems.edge.evcs.keba.kecontact.core.KebaKeContactCore;

@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Evcs.Keba.KeContact", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE)
public class KebaKeContact extends AbstractOpenemsComponent
		implements Evcs, OpenemsComponent, EventHandler, ModbusSlave {

	public final static int UDP_PORT = 7090;

	private final Logger log = LoggerFactory.getLogger(KebaKeContact.class);
	private final ReadWorker readWorker = new ReadWorker(this);
	private final ReadHandler readHandler = new ReadHandler(this);
	private final WriteHandler writeHandler = new WriteHandler(this);
	private Boolean lastConnectionLostState = false;

	@Reference(policy = ReferencePolicy.STATIC, cardinality = ReferenceCardinality.MANDATORY)
	private KebaKeContactCore kebaKeContactCore = null;

	
	public KebaKeContact() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Evcs.ChannelId.values(), //
				KebaChannelId.values() //
		);
	}

	private InetAddress ip = null;

	@Activate
	void activate(ComponentContext context, Config config) throws UnknownHostException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.ip = Inet4Address.getByName(config.ip());

		/*
		 * subscribe on replies to report queries
		 */
		this.kebaKeContactCore.onReceive((ip, message) -> {
			if (ip.equals(this.ip)) { // same IP -> handle message
				this.readHandler.accept(message);
				this.channel(KebaChannelId.ChargingStation_COMMUNICATION_FAILED).setNextValue(false);
			}
		});

		// start queryWorker
		this.readWorker.activate(this.id() + "query");
		
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
		this.readWorker.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:

			// Clear channels if the connection to the Charging Station has been lost
			Channel<Boolean> connectionLostChannel = this.channel(KebaChannelId.ChargingStation_COMMUNICATION_FAILED);
			Boolean connectionLost = connectionLostChannel.value().orElse(lastConnectionLostState);
			if (connectionLost != lastConnectionLostState) {
				if (connectionLost) {
					resetChannelValues();
				}
				lastConnectionLostState = connectionLost;
			}

			// handle writes
			this.writeHandler.run();
			break;
		}
	}

	/**
	 * Send UDP message to KEBA KeContact. Returns true if sent successfully
	 *
	 * @param s
	 * @return
	 */
	protected boolean send(String s) {
		byte[] raw = s.getBytes();
		DatagramPacket packet = new DatagramPacket(raw, raw.length, ip, KebaKeContact.UDP_PORT);
		DatagramSocket dSocket = null;
		try {
			dSocket = new DatagramSocket();
			this.logInfo(this.log, "Sending message to KEBA KeContact [" + s + "]");
			dSocket.send(packet);
			return true;
		} catch (SocketException e) {
			this.logError(this.log, "Unable to open UDP socket for sending [" + s + "] to [" + ip.getHostAddress()
					+ "]: " + e.getMessage());
		} catch (IOException e) {
			this.logError(this.log,
					"Unable to send [" + s + "] UDP message to [" + ip.getHostAddress() + "]: " + e.getMessage());
		} finally {
			if (dSocket != null) {
				dSocket.close();
			}
		}
		return false;
	}

	/**
	 * Triggers an immediate execution of query reports
	 */
	protected void triggerQuery() {
		this.readWorker.triggerNextRun();
	}

	@Override
	protected void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	public String debugLog() {
		return "Limit:" + this.channel(KebaChannelId.CURR_USER).value().asString();
	}

	public ReadWorker getReadWorker() {
		return readWorker;
	}

	public ReadHandler getReadHandler() {
		return readHandler;
	}

	/**
	 * Resets all channel values except 
	 * the Communication_Failed channel
	 */
	private void resetChannelValues() {
		for (KebaChannelId c : KebaChannelId.values()) {
			if (c != KebaChannelId.ChargingStation_COMMUNICATION_FAILED) {
				Channel<?> channel = this.channel(c);
				channel.setNextValue(null);
			}
		}

	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		
		return new ModbusSlaveTable(
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), 
				Evcs.getModbusSlaveNatureTable(accessMode),
				this.getModbusSlaveNatureTable(accessMode)
				
		);
	}
	
	private ModbusSlaveNatureTable getModbusSlaveNatureTable(AccessMode accessMode) {
		
		return ModbusSlaveNatureTable.of(KebaKeContact.class, accessMode, 300) //
		
				
		.channel(0, KebaChannelId.PRODUCT, ModbusType.STRING16)
		.channel(16, KebaChannelId.SERIAL, ModbusType.STRING16)
		.channel(32, KebaChannelId.FIRMWARE, ModbusType.STRING16)
		.channel(48, KebaChannelId.COM_MODULE, ModbusType.STRING16)
		.channel(64, KebaChannelId.STATUS, ModbusType.UINT16)
		.channel(65, KebaChannelId.ERROR_1, ModbusType.UINT16)
		.channel(66, KebaChannelId.ERROR_2, ModbusType.UINT16)
		.channel(67, KebaChannelId.PLUG, ModbusType.UINT16)
		.channel(68, KebaChannelId.ENABLE_SYS, ModbusType.UINT16)
		.channel(69, KebaChannelId.ENABLE_USER, ModbusType.UINT16)
		.channel(70, KebaChannelId.MAX_CURR_PERCENT, ModbusType.UINT16)
		.channel(71, KebaChannelId.CURR_USER, ModbusType.UINT16)
		.channel(72, KebaChannelId.CURR_FAILSAFE, ModbusType.UINT16)
		.channel(73, KebaChannelId.TIMEOUT_FAILSAFE, ModbusType.UINT16)
		.channel(74, KebaChannelId.CURR_TIMER, ModbusType.UINT16)
		.channel(75, KebaChannelId.TIMEOUT_CT, ModbusType.UINT16)
		.channel(76, KebaChannelId.ENERGY_LIMIT, ModbusType.UINT16)
		.channel(77, KebaChannelId.OUTPUT, ModbusType.UINT16)
		.channel(78, KebaChannelId.INPUT, ModbusType.UINT16)
		 
		//Report 3
		.channel(79, KebaChannelId.VOLTAGE_L1, ModbusType.UINT16)
		.channel(80, KebaChannelId.VOLTAGE_L2, ModbusType.UINT16)
		.channel(81, KebaChannelId.VOLTAGE_L3, ModbusType.UINT16)
		.channel(82, KebaChannelId.CURRENT_L1, ModbusType.UINT16)
		.channel(83, KebaChannelId.CURRENT_L2, ModbusType.UINT16)
		.channel(84, KebaChannelId.CURRENT_L3, ModbusType.UINT16)
		.channel(85, KebaChannelId.ACTUAL_POWER, ModbusType.UINT16)
		.channel(86, KebaChannelId.COS_PHI, ModbusType.UINT16)
		.channel(87, KebaChannelId.ENERGY_SESSION, ModbusType.UINT16)
		.channel(88, KebaChannelId.ENERGY_TOTAL, ModbusType.UINT16)
		.channel(89, KebaChannelId.PHASES, ModbusType.UINT16)
		.uint16Reserved(90)
		.channel(91, KebaChannelId.ChargingStation_COMMUNICATION_FAILED, ModbusType.UINT16)
		.build();
	}
}
