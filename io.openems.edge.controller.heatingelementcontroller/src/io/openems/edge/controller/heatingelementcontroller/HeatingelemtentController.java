package io.openems.edge.controller.heatingelementcontroller;

import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.sum.Sum;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.controller.api.Controller;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Heatingelement", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HeatingelemtentController extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(HeatingelemtentController.class);

	private int l1;
	private int l2;
	private int l3;
	private int hysteresis;
	private ChannelAddress l1_outputChannelAddress;
	private ChannelAddress l2_outputChannelAddress;
	private ChannelAddress l3_outputChannelAddress;

	@Reference
	protected ComponentManager componentManager;

	@Reference
	protected ConfigurationAdmin cm;

	public HeatingelemtentController() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Reference
	private Sum sum;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		L1_ACTIVATION(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)
				.text("Activates the Phase if the current SoC reaches this value")), //
		L2_ACTIVATION(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)
				.text("Activates the Phase if the current SoC reaches this value")), //
		L3_ACTIVATION(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)
				.text("Activates the Phase if the current SoC reaches this value")), //
		HYSTERESIS(Doc.of(OpenemsType.INTEGER)
				.text("Dectivates the Phase if the current SoC fell under the Activate value - Hysteresis")), //
		STATUS(Doc.of(State.values()).text("Current state of the Heating Element"));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		super.activate(context, config.id(), config.enabled());

		this.channel(ChannelId.L1_ACTIVATION).setNextValue(config.l1Activation());
		this.channel(ChannelId.L2_ACTIVATION).setNextValue(config.l2Activation());
		this.channel(ChannelId.L3_ACTIVATION).setNextValue(config.l3Activation());
		this.channel(ChannelId.HYSTERESIS).setNextValue(config.hysteresis());

		l1 = config.l1Activation();
		l2 = config.l2Activation();
		l3 = config.l3Activation();
		hysteresis = config.hysteresis();
		l1_outputChannelAddress = ChannelAddress.fromString(config.l1OutputChannelAddress());
		l2_outputChannelAddress = ChannelAddress.fromString(config.l2OutputChannelAddress());
		l3_outputChannelAddress = ChannelAddress.fromString(config.l3OutputChannelAddress());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {

		int soc = this.sum.getEssSoc().value().orElse(0);

		Channel<State> status_channel = this.channel(ChannelId.STATUS);
		State status = status_channel.value().asEnum();
		System.out.println(status);
		if(status==State.OFF) {
			System.out.println("geht"+status);
		}
		switch (status) {
		case L1:

			if (soc >= l2) {
				setState(State.L2);
				setOutput(l2_outputChannelAddress, true);
			} else {
				if (soc < l1 - hysteresis) {
					setState(State.OFF);
					setOutput(l1_outputChannelAddress, false);
				}
			}
			break;

		case L2:

			if (soc >= l3) {
				setState(State.L3);
				setOutput(l3_outputChannelAddress, true);
			} else {
				if (soc < l2 - hysteresis) {
					setState(State.L1);
					setOutput(l2_outputChannelAddress, false);
				}
			}
			break;
		case L3:

			if (soc < l3 - hysteresis) {
				setState(State.L2);
				setOutput(l3_outputChannelAddress, false);
			}

			break;
		case OFF:
			proofSocAndSetNewState(soc);
			break;
		}

	}

	private void setState(State newState) {

		this.channel(ChannelId.STATUS).setNextValue(newState);

	}

	private void proofSocAndSetNewState(int soc) throws IllegalArgumentException, OpenemsNamedException {

		if (soc >= l1) {
			if (soc >= l2) {
				if (soc >= l3) {
					this.channel(ChannelId.STATUS).setNextValue(State.L3);
					setOutput(l3_outputChannelAddress, true);
				}
				this.channel(ChannelId.STATUS).setNextValue(State.L2);
				setOutput(l2_outputChannelAddress, true);
			}
			this.channel(ChannelId.STATUS).setNextValue(State.L1);
			setOutput(l1_outputChannelAddress, true);
		} else {
			this.channel(ChannelId.STATUS).setNextValue(State.OFF);
		}
	}

	private void setOutput(ChannelAddress address, boolean value)
			throws IllegalArgumentException, OpenemsNamedException {
		try {
			WriteChannel<Boolean> outputChannel = this.componentManager.getChannel(address);
			Optional<Boolean> currentValueOpt = outputChannel.value().asOptional();
			if (!currentValueOpt.isPresent() || currentValueOpt.get()) {
				this.logInfo(this.log, "Set output [" + outputChannel.address() + "] " + (value ? "ON" : "OFF") + ".");
				outputChannel.setNextWriteValue(value);
			}
		} catch (OpenemsException e) {
			this.logError(this.log, "Unable to set output: [" + address + "] " + e.getMessage());
		}
	}

}
