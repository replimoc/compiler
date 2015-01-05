package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class AddOperation extends StorageRegisterOperation {

	public AddOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public AddOperation(Bit mode, Storage input, Register destinationRegister) {
		super(null, mode, input, destinationRegister);
	}

	public AddOperation(String comment, Bit mode, Storage input, Register destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tadd%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
