package io.openems.edge.controller.heatingelementcontroller;

import io.openems.common.types.OptionsEnum;

public enum State implements OptionsEnum {

	OFF(-1, "Off"),
	/**
	 * Value is smaller than threshold1.
	 */
	L1(0, "Digital output ON"),
	/**
	 * Value is larger threshold1 but smaller than threshold2.
	 */
	L2(1, "Digital output ON"),
	/**
	 * Value is larger threshold2 but smaller than threshold3.
	 */
	L3(2, "Digital output ON");
	/**
	 * Value is larger threshold3.
	 */

	private final int value;
	private final String name;

	private State(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return OFF;
	}

}
