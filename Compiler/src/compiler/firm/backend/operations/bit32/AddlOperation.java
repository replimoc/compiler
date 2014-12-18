package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class AddlOperation extends TwoRegOperandsOperation {

	public AddlOperation() {
		super(null);
	}

	public AddlOperation(String comment) {
		super(comment);
	}

	public AddlOperation(Register input, Register destinationRegister) {
		super(null, input, destinationRegister);
	}

	public AddlOperation(String comment, Register input, Register destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\taddl %s, %s", getInputRegister().toString32(), getDestinationRegister().toString32());
	}
}
