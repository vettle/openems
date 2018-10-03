package io.openems.edge.battery.microcare.mk1;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Microcare BMS MK1", //
        description = "Provides a service for connecting to, querying and writing to a MC-Comms enabled BMS device.")

@interface Config {
    String service_pid();

    String id() default "bms0";

    boolean enabled() default true;

    @AttributeDefinition(name = "Slave-Address", description = "Desired address of this MC-Comms slave device")
    int slaveAddress() default 2;

    @AttributeDefinition(name = "Bridge ID", description = "MC-Comms master controlling this slave device")
    String bridgeID();
}
