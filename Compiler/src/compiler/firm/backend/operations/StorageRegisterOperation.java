package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerOperation {

	private final Storage storage;
	private final Register destination;

	public StorageRegisterOperation(Storage storage, Register destination) {
		this.storage = storage;
		this.destination = destination;
	}

	public Storage getStorage() {
		return storage;
	}

	public Register getDestination() {
		return destination;
	}
}
