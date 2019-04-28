package io.openems.edge.controller.symmetric.avoidtotaldischargesoctimeline;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Controller IO Fix Digital Output", //
		description = "This controller sets a digital output channel according to the given value")
@interface Config {

	String id() default "ess0";

	boolean enabled() default true;

	@AttributeDefinition(name = "Soc Timeline", description = "This option configures an minsoc at a time for an ess. If no minsoc for an ess is configured the controller uses the minsoc of the ess.")
	String socTimeline();

	@AttributeDefinition(name = "Next Discharge", description = "Next Time, the ess will discharge completely.")
	String nextDischarge() default "2018-03-09";

	@AttributeDefinition(name = "Discharge Period", description = "The Period of time between two Discharges.https://docs.oracle.com/javase/8/docs/api/java/time/Period.html#parse-java.lang.CharSequence-")
	String dischargePeriod() default "P4W";

	@AttributeDefinition(name = "Discharge Start Time", description = "The time of the Day to start Discharging.")
	String dischargeStartTime() default "12:00:00";

	@AttributeDefinition(name = "Enable Discharge ", description = "This option allowes the system to discharge the ess according to the nextDischarge completely. This improves the soc calculation.")
	boolean enableDischarge() default false;

	String webconsole_configurationFactory_nameHint() default "Controller IO FixDigitalOutput [{id}]";
}