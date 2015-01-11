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
		return storage.getUsedRegister();
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {};
	}
}
