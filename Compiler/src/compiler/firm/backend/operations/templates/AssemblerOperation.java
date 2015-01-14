package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;

public abstract class AssemblerOperation {

	private static final Register[] accumulatorRegisters = { Register._10D, Register._11D };

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

	protected Register getTemporaryRegister() {
		if (accumulatorRegister >= accumulatorRegisters.length) {
			throw new RuntimeException("Running out of accumulator registers");
		}
		return accumulatorRegisters[accumulatorRegister++];
	}
}
