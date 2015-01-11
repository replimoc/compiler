package compiler.firm.backend.operations.templates;

import java.util.Arrays;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends AssemblerBitOperation {

	private final Storage storage;
	private final RegisterBased destination;

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

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased[] storageRegister = storage.getUsedRegister();
		if (storageRegister != null) {
			RegisterBased[] result = Arrays.copyOf(storageRegister, storageRegister.length + 1);
			result[storageRegister.length] = this.destination;
			return result;
		} else {
			return new RegisterBased[] { this.destination };
		}
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { this.destination };
	}
}
