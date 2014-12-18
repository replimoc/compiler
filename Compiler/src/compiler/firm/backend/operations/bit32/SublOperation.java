package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class SublOperation extends TwoRegOperandsOperation {

	public SublOperation() {
		super(null);
	}

	public SublOperation(String comment) {
		super(comment);
	}

	public SublOperation(Register input, Register destinationRegister) {
		super(null, input, destinationRegister);
	}

	public SublOperation(String comment, Register input, Register destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsubl %s, %s", getInputRegister().toString32(), getDestinationRegister().toString32());
	}
}
