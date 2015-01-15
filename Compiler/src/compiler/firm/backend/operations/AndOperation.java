package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AndOperation extends StorageRegisterOperation {

	public AndOperation(Storage storage, RegisterBased destination) {
		super(null, storage, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tand %s, %s", getStorage().toString(), getDestination().toString());
	}
}
