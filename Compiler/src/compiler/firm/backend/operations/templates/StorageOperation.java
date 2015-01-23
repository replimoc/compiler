package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

public abstract class StorageOperation extends AssemblerBitOperation {
	private final Storage storage;

	public StorageOperation(Bit mode, Storage storage) {
		this(null, mode, storage);
	}

	public StorageOperation(String comment, Bit mode, Storage storage) {
		super(comment);
		this.storage = storage;
	}

	public Storage getStorage() {
		return storage;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(storage.getUsedRegister());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}
}
