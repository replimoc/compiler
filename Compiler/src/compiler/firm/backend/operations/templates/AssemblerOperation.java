package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;

public abstract class AssemblerOperation {

	private static final RegisterBundle[] ACCUMULATOR_REGISTERS = { RegisterBundle._13D, RegisterBundle._14D, RegisterBundle._15D };

	private final String comment;
	private int accumulatorRegister = 0;

	public AssemblerOperation() {
		this.comment = null;
	}

	public AssemblerOperation(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public final String toString() {
		String operationString = getOperationString();
		return getComment() == null ? operationString : operationString + "\t# " + getComment();
	}

	public String[] toStringWithSpillcode() {
		return new String[] { toString() };
	}

	public abstract String getOperationString();

	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] {};
	}

	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {};
	}

	public boolean hasSpilledRegisters() {
		for (RegisterBased register : getReadRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		for (RegisterBased register : getWriteRegisters()) {
			if (register.isSpilled()) {
				return true;
			}
		}
		return false;
	}

	protected RegisterBundle getTemporaryRegister() {
		if (accumulatorRegister >= ACCUMULATOR_REGISTERS.length) {
			throw new RuntimeException("Running out of accumulator registers");
		}
		return ACCUMULATOR_REGISTERS[accumulatorRegister++];
	}
}
