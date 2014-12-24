package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerOperation {

	private Bit mode;
	private Storage storage;
	private Register destination;

	public StorageRegisterOperation(String comment, Bit mode) {
		super(comment);
		this.mode = mode;
	}

	public StorageRegisterOperation(String comment, Bit mode, Storage storage, Register destination) {
		super(comment);
		this.storage = storage;
		this.destination = destination;
		this.mode = mode;
	}

	public Storage getStorage() {
		return storage;
	}

	public Register getDestination() {
		return destination;
	}

	public Bit getMode() {
		return mode;
	}

	public void initialize(Storage storage, Register destination) {
		this.storage = storage;
		this.destination = destination;
	}
}
