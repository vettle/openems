/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.edge.controller.symmetric.avoidtotaldischargesoctimeline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Power;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.PvInverter.FixPowerLimit", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class AvoidTotalDischargeSocTimeLineController extends AbstractOpenemsComponent
		implements ManagedSymmetricEss, SymmetricEss, Controller, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(AvoidTotalDischargeSocTimeLineController.class);

	private LocalDate nextDischargeDate;
	private LocalTime dischargeStartTime;
	private LocalDateTime dischargeStart;
	private Config config;

	@Reference
	private Power power = null;

	private final List<SymmetricEss> esss = new CopyOnWriteArrayList<>();
	private final List<ManagedSymmetricEss> managedEsss = new CopyOnWriteArrayList<>();

	@Reference
	protected ConfigurationAdmin cm;

	@Reference
	protected ComponentManager componentManager;

	public AvoidTotalDischargeSocTimeLineController() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Reference( //
			policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY, //
			cardinality = ReferenceCardinality.MULTIPLE, //
			target = "(enabled=true)")
	protected void addEss(SymmetricEss ess) {
		// Do not add myself
		if (ess == this) {
			return;
		}

		this.esss.add(ess);
		if (ess instanceof ManagedSymmetricEss) {
			this.managedEsss.add((ManagedSymmetricEss) ess);
		}
	}

	protected void removeEss(SymmetricEss ess) {
		if (ess == this) {
			return;
		}

		this.esss.remove(ess);
		if (ess instanceof ManagedSymmetricEss) {
			this.managedEsss.remove((ManagedSymmetricEss) ess);
		}
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.enabled());
		this.config = config;
		// update filter for 'esss' component
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "esss", config.ess_ids())) {
			return;
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {

		for (SymmetricEss ess : this.esss) {
		}

	}

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		;
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	@Override
	public Power getPower() {
		return this.power;
	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		throw new OpenemsException("AvoidTotalDischargeSocTimeline.applyPower() should never be called.");
	}

	@Override
	public int getPowerPrecision() {
		return 0;
	}

}
