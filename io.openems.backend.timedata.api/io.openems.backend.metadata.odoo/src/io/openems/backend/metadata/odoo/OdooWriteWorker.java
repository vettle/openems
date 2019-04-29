package io.openems.backend.metadata.odoo;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsException;

/**
 * This worker combines writes to lastMessage and lastUpdate fields, to avoid
 * DDOSing Odoo by writing too often.
 * 
 * @author stefan.feilmeier
 *
 */
public class OdooWriteWorker {

	private final static int UPDATE_INTERVAL_IN_SECONDS = 60;

	private final Logger log = LoggerFactory.getLogger(OdooWriteWorker.class);
	private final Odoo parent;

	/**
	 * Holds the scheduled task
	 */
	private ScheduledFuture<?> future = null;

	/**
	 * Executor for subscriptions task
	 */
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	public OdooWriteWorker(Odoo parent) {
		this.parent = parent;
	}

	public synchronized void start(OdooCredentials odooCredentials) {
		this.future = this.executor.scheduleWithFixedDelay(//
				() -> task.accept(odooCredentials), //
				0, UPDATE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
	}

	public synchronized void stop() {
		// unsubscribe regular task
		if (this.future != null) {
			this.future.cancel(true);
		}
		// Shutdown executor
		if (this.executor != null) {
			try {
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				this.parent.logWarn(this.log, "tasks interrupted");
			} finally {
				if (!executor.isTerminated()) {
					this.parent.logWarn(this.log, "cancel non-finished tasks");
				}
				executor.shutdownNow();
			}
		}
	}

	private Consumer<OdooCredentials> task = (odooCredentials) -> {
		/*
		 * This task is executed regularly. Sends data to websocket.
		 */
		String time = OdooUtils.DATETIME_FORMATTER.format(ZonedDateTime.now(ZoneOffset.UTC));
		{
			Integer[] ids;
			synchronized (this.lastMessageOdooIds) {
				ids = this.lastMessageOdooIds.toArray(new Integer[this.lastMessageOdooIds.size()]);
				this.lastMessageOdooIds.clear();
			}
			if (ids.length > 0) {
				try {
					OdooUtils.write(odooCredentials, Odoo.ODOO_MODEL, ids,
							new FieldValue(Field.EdgeDevice.LAST_MESSAGE, time));
				} catch (OpenemsException e) {
					log.error("Unable to write lastMessage: " + e.getMessage());
				}
			}
		}
		{
			Integer[] ids;
			synchronized (this.lastUpdateOdooIds) {
				ids = this.lastUpdateOdooIds.toArray(new Integer[this.lastUpdateOdooIds.size()]);
				this.lastUpdateOdooIds.clear();
			}
			if (ids.length > 0) {
				try {
					OdooUtils.write(odooCredentials, Odoo.ODOO_MODEL, ids,
							new FieldValue(Field.EdgeDevice.LAST_UPDATE, time));
				} catch (OpenemsException e) {
					log.error("Unable to write lastUpdate: " + e.getMessage());
				}
			}
		}
	};

	private final Set<Integer> lastMessageOdooIds = new HashSet<>();
	private final Set<Integer> lastUpdateOdooIds = new HashSet<>();

	public void onLastMessage(MyEdge edge) {
		synchronized (this.lastMessageOdooIds) {
			this.lastMessageOdooIds.add(edge.getOdooId());
		}
	}

	public void onLastUpdate(MyEdge edge) {
		synchronized (this.lastUpdateOdooIds) {
			this.lastUpdateOdooIds.add(edge.getOdooId());
		}
	}

}
