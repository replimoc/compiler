package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerOperation {

	private final Storage storage;
	private final Register destination;

	public StorageRegisterOperation(String comment, Storage storage, Register destination) {
		super(comment);
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
