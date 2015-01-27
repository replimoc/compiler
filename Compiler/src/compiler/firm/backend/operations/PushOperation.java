package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageOperation;
import compiler.firm.backend.storage.Storage;

public class PushOperation extends StorageOperation {

	public PushOperation(Storage storage) {
		this(null, storage);
	}

	public PushOperation(String comment, Storage storage) {
		super(comment, storage);
	}

	@Override
	public String getOperationString() {
		return String.format("\tpush %s", getStorage().toString());
	}
}
