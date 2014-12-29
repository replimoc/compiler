package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerBitOperation {

	private Storage storage;
	private RegisterBased destination;

	public StorageRegisterOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public StorageRegisterOperation(String comment, Bit mode, Storage storage, RegisterBased destination) {
		super(comment, mode);
		this.storage = storage;
		this.destination = destination;
	}

	public Storage getStorage() {
		return storage;
	}

	public RegisterBased getDestination() {
		return destination;
	}

	public void initialize(Storage storage, RegisterBased destination) {
		this.storage = storage;
		this.destination = destination;
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		RegisterBased storageRegister = storage.getUsedRegister();
		if (storageRegister != null) {
			return new RegisterBased[] { storageRegister, this.destination };
		} else {
			return new RegisterBased[] { this.destination };
		}
	}
}
