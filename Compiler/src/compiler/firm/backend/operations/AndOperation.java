package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AndOperation extends StorageRegisterOperation {

	public AndOperation(Bit mode, Storage storage, RegisterBased destination) {
		super(null, mode, storage, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tand%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
