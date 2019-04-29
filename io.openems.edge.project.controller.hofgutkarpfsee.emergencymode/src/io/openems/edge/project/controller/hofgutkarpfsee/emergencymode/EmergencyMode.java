package io.openems.edge.project.controller.hofgutkarpfsee.emergencymode;

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

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.SymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.EmergencyMode", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class EmergencyMode extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(EmergencyMode.class);
	private Config config;

	@Reference
	protected ComponentManager componentManager;

	@Reference
	protected ConfigurationAdmin cm;

	protected EmergencyMode() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ThisChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		super.activate(context, config.id(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		switch (this.getGridMode()) {
		case UNDEFINED:
			/*
			 * Grid-Mode is undefined -> wait till we have some clear information
			 */
			break;

		case OFF_GRID:
			/*
			 * Both ESS are Off-Grid
			 */
			this.handleOffGridState();
			break;

		case ON_GRID:
			/*
			 * Both ESS are On-Grid
			 */
			this.handleOnGridState();
			break;
		}
	}

	private void handleOnGridState() {
		// TODO
	}

	private void handleOffGridState() {
		// TODO
	}

	/**
	 * Gets the Grid-Mode of both ESS.
	 * 
	 * @return the Grid-Mode
	 */
	private GridMode getGridMode() {
		SymmetricEss ess1;
		try {
			ess1 = this.componentManager.getComponent(this.config.ess1_id());
		} catch (OpenemsNamedException e) {
			return GridMode.UNDEFINED;
		}

		GridMode ess1GridMode = ess1.getGridMode().value().asEnum();
		if (
		// At least Ess1 is On-Grid
		(ess1GridMode == GridMode.ON_GRID)) {
			return GridMode.ON_GRID;
		} else if (
		// At least Ess1 is Off-Grid
		(ess1GridMode == GridMode.OFF_GRID)) {
			// At least Ess2 is Off-Grid
			return GridMode.OFF_GRID;
		} else {
			return GridMode.UNDEFINED;
		}
	}

}
