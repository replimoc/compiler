package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmovSignOperation extends StorageRegisterOperation {

	public CmovSignOperation(String comment, Storage source, RegisterBased destination) {
		super(comment, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmovs %s, %s", source, destination);
	}
}