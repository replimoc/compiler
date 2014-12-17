package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class AndqOperation extends StorageRegisterOperation {

	public AndqOperation(Storage storage, Register destination) {
		super(storage, destination);
	}

	@Override
	public String toString() {
		return String.format("\tandq %s, %s\n", getStorage(), getDestination());
	}
}
