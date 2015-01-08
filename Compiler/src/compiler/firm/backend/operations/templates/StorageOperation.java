package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class StorageOperation extends AssemblerBitOperation {
	private final Storage storage;

	public StorageOperation(Bit mode, Storage storage) {
		this(null, mode, storage);
	}

	public StorageOperation(String comment, Bit mode, Storage storage) {
		super(comment, mode);
		this.storage = storage;
	}

	public Storage getStorage() {
		return storage;
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased storageRegister = storage.getUsedRegister();
		if (storageRegister != null) {
			return new RegisterBased[] { storageRegister };
		} else {
			return new RegisterBased[] {};
		}
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {};
	}
}
