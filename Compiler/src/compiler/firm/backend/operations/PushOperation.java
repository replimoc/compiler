package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageOperation;
import compiler.firm.backend.storage.Storage;

public class PushOperation extends StorageOperation {

	public PushOperation(Bit mode, Storage storage) {
		this(null, mode, storage);
	}

	public PushOperation(String comment, Bit mode, Storage storage) {
		super(comment, mode, storage);
	}

	@Override
	public String getOperationString() {
		return String.format("\tpush %s", getStorage().toString());
	}
}
