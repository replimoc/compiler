package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class SubqOperation extends StorageRegisterOperation {

	public SubqOperation(Storage storage, Register destination) {
		super(storage, destination);
	}

	@Override
	public String toString() {
		return String.format("\tsubq %s, %s\n", getStorage().toString64(), getDestination().toString64());
	}
}
