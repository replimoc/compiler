package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class AddqOperation extends StorageRegisterOperation {

	public AddqOperation() {
		super(null);
	}

	public AddqOperation(String comment) {
		super(comment);
	}

	public AddqOperation(Storage input, Register destinationRegister) {
		super(null, input, destinationRegister);
	}

	public AddqOperation(String comment, Storage input, Register destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\taddq %s, %s", getStorage().toString64(), getDestination().toString64());
	}
}
