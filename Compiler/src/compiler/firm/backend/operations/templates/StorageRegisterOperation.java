package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerOperation {

	private Storage storage;
	private Register destination;

	public StorageRegisterOperation(String comment) {
		super(comment);
	}

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

	public void initialize(Storage storage, Register destination) {
		this.storage = storage;
		this.destination = destination;
	}
}
