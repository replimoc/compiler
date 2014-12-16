package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public abstract class ConstantRegisterOperation extends AssemblerOperation {

	private final String constant;
	private final Register destination;

	public ConstantRegisterOperation(long constant, Register destination) {
		this(String.format("%x", constant), destination);
	}

	public ConstantRegisterOperation(String constant, Register destination) {
		this.constant = constant;
		this.destination = destination;
	}

	public String getConstant() {
		return this.constant;
	}

	public Register getDestination() {
		return destination;
	}
}
