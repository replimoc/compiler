package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class AndqOperation extends StorageRegisterOperation {

	public AndqOperation(Storage storage, Register destination) {
		super(null, storage, destination);
	}

	public AndqOperation(String comment, Storage storage, Register destination) {
		super(comment, storage, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tandq %s, %s", getStorage().toString64(), getDestination().toString64());
	}
}
