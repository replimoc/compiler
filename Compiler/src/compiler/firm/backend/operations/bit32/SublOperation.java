package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class SublOperation extends StorageRegisterOperation {

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
		return String.format("\tsubl %s, %s", getStorage().toString32(), getDestination().toString32());
	}
}
