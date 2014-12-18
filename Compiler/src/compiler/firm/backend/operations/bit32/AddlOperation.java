package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class AddlOperation extends StorageRegisterOperation {

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
		return String.format("\taddl %s, %s", getStorage().toString32(), getDestination().toString32());
	}
}
