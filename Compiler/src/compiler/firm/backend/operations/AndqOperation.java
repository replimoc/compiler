package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class AndqOperation extends ConstantRegisterOperation {

	public AndqOperation(long constant, Register destination) {
		super(constant, destination);
	}

	public AndqOperation(String constant, Register destination) {
		super(constant, destination);
	}

	@Override
	public String toString() {
		return String.format("\tandq $0x%s, %s\n", getConstant(), getDestination());
	}
}
