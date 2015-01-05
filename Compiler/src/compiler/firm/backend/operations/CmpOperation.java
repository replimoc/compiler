package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class CmpOperation extends StorageRegisterOperation {

	public CmpOperation(String comment, Bit mode, RegisterBased input, RegisterBased destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp %s, %s", getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
