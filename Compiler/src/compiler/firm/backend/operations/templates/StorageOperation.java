package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

public abstract class StorageOperation extends AssemblerBitOperation {
	private final Storage storage;

	public StorageOperation(Storage storage) {
		this(null, storage);
	}

	public StorageOperation(String comment, Storage storage) {
		super(comment);
		this.storage = storage;
	}

	public Storage getStorage() {
		return storage;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(storage.getReadRegisters());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}
}
