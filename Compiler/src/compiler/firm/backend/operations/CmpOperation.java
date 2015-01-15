package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmpOperation extends StorageRegisterOperation {

	public CmpOperation(String comment, Storage input, RegisterBased destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp%s %s, %s", getDestination().getMode(), getStorage().toString(), getDestination().toString());
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[0];
	}
}
