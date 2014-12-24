package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerBitOperation {

	private Storage storage;
	private Register destination;

	public StorageRegisterOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public StorageRegisterOperation(String comment, Bit mode, Storage storage, Register destination) {
		super(comment, mode);
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
