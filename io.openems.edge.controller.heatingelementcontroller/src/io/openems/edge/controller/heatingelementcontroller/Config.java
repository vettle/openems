package io.openems.edge.controller.heatingelementcontroller;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( 
		name = "Controller Heatingelement", //
		description = "This controller switches the different Phases of a Heatingelement ON and OFF in relation to the State of Charge.")
@interface Config {
	String id() default "heating0"; 

	boolean enabled() default true; 
	
	@AttributeDefinition(name = "Output Channel (L1)", description = "Channel address of the Digital Output of Phase 1 that should be switched")
	String l1OutputChannelAddress();
	
	@AttributeDefinition(name = "Output Channel (L2)", description = "Channel address of the Digital Output of Phase 2 that should be switched")
	String l2OutputChannelAddress();
	
	@AttributeDefinition(name = "Output Channel (L3)", description = "Channel address of the Digital Output of Phase 3 that should be switched")
	String l3OutputChannelAddress();
	
	@AttributeDefinition(name ="L1 Threshold", description ="Activates the 1. Phase if the current SoC reaches this value")
	int l1Activation() default 92;
	
	@AttributeDefinition(name ="L2 Threshold", description ="Activates the 2. Phase if the current SoC reaches this value")
	int l2Activation() default 94;
	
	@AttributeDefinition(name ="L3 Threshold", description ="Activates the 3. Phase if the current SoC reaches this value")
	int l3Activation() default 96;
	
	@AttributeDefinition(name ="Hysteresis", description ="Dectivates the Phase if the current SoC fell under the Activate value - Hysteresis")
	int hysteresis() default 5;
	
	String webconsole_configurationFactory_nameHint() default "Heatingelement Controller[{id}]"; 
}
