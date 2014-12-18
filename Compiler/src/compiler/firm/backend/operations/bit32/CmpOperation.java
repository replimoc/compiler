package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.storage.Register;

public class CmpOperation extends TwoRegOperandsOperation {

	public CmpOperation() {
		super(null);
	}

	public CmpOperation(String comment) {
		super(comment);
	}

	public CmpOperation(Register input, Register destinationRegister) {
		super(null, input, destinationRegister);
	}

	public CmpOperation(String comment, Register input, Register destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp %s, %s", getInputRegister().toString32(), getDestinationRegister().toString32());
	}
}
