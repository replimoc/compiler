package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class ImullOperation extends TwoRegOperandsOperation {

	public ImullOperation() {
	}

	public ImullOperation(Register input, Register destinationRegister) {
		super(input, destinationRegister);
	}

	@Override
	public String toString() {
		return String.format("\timull %s, %s\n", getInputRegister().toString32(), getDestinationRegister().toString32());
	}
}
