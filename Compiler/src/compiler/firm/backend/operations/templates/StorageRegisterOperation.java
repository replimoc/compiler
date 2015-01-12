package compiler.firm.backend.operations.templates;

import java.util.Arrays;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterOperation extends SourceDestinationOperation {

	public StorageRegisterOperation(String comment, Bit mode, Storage storage, RegisterBased destination) {
		super(comment, mode, storage, destination);
	}

	public Storage getStorage() {
		return getSource();
	}

	@Override
	public RegisterBased getDestination() {
		return (RegisterBased) super.getDestination();
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased[] storageRegister = getSource().getUsedRegister();
		if (storageRegister != null) {
			RegisterBased[] result = Arrays.copyOf(storageRegister, storageRegister.length + 1);
			result[storageRegister.length] = getDestination();
			return result;
		} else {
			return new RegisterBased[] { getDestination() };
		}
	}
}
