package io.openems.edge.ess.power.api;

import io.openems.common.types.OptionsEnum;

public enum SolverStrategy implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	NONE(-1, "None"), //
	ALL_CONSTRAINTS(0, "All Constraints"), //
	OPTIMIZE_BY_MOVING_TOWARDS_TARGET(1, "Optimize By Moving Towards Target"), //
	OPTIMIZE_BY_KEEPING_TARGET_DIRECTION_AND_MAXIMIZING_IN_ORDER(2,
			"Optimize By Keeping Target Direction And Maximizing In Order");

	private final int value;
	private final String name;

	private SolverStrategy(int value, String name) {
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
		return UNDEFINED;
	}
}
