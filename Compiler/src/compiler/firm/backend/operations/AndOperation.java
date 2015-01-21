package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AndOperation extends StorageRegisterRegisterOperation {

	public AndOperation(Storage source1, RegisterBased source2, RegisterBased destination) {
		super(null, source1, source2, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tand %s, %s", getSource().toString(), getDestination().toString());
	}
}
