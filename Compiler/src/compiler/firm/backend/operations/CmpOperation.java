package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmpOperation extends StorageRegisterOperation {

	public CmpOperation(String comment, Bit mode, Storage input, RegisterBased destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[0];
	}
}
