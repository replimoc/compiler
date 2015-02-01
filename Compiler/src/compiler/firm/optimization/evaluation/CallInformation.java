package compiler.firm.optimization.evaluation;

import firm.Entity;

public class CallInformation {
	private final Entity otherEntity;
	private final int constantArguments;

	public CallInformation(Entity otherEntity, int constantArguments) {
		this.otherEntity = otherEntity;
		this.constantArguments = constantArguments;
	}

	public int getConstantArguments() {
		return constantArguments;
	}

	public Entity getOtherEntity() {
		return otherEntity;
	}
}
