package io.openems.edge.project.controller.hofgutkarpfsee.emergencymode;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Controller Emergency Mode", //
		description = "TODO.")
@interface Config {
	String id() default "ctrlEmergencyMode0";

	boolean enabled() default true;

	/*
	 * WAGO
	 */
	// Q1 - ess1
	@AttributeDefinition(name = "", description = "")
	String channelAddress();

	/*
	 * Meters
	 */
	@AttributeDefinition(name = "", description = "")
	String meter_id();

	/*
	 * Ess
	 */
	@AttributeDefinition(name = "Ess1-ID", description = "ID of Ess1.")
	String ess1_id();

	String webconsole_configurationFactory_nameHint() default "Controller Emergency Mode [{id}]";
}
