package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class SubqOperation extends StorageRegisterOperation {

	public SubqOperation(Storage storage, Register destination) {
		super(storage, destination);
	}

	@Override
	public String toString() {
		return String.format("\tsubq %s, %s\n", getStorage(), getDestination());
	}
}
