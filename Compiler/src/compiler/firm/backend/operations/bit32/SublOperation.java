package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class SublOperation extends TwoRegOperandsOperation {

	public SublOperation() {
	}

	public SublOperation(Register input, Register destinationRegister) {
		super(input, destinationRegister);
	}

	@Override
	public String toString() {
		return String.format("\tsubl %s, %s\n", getInputRegister(), getDestinationRegister());
	}

	public static TwoRegOperandsOperation generate(Register input, Register destination) {
		return new SublOperation(input, destination);
	}
}
