package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class PushOperation extends AssemblerBitOperation {

	private final Storage storage;

	public PushOperation(Bit mode, Storage storage) {
		this(null, mode, storage);
	}

	public PushOperation(String comment, Bit mode, Storage storage) {
		super(comment, mode);
		this.storage = storage;
	}

	@Override
	public String getOperationString() {
		return String.format("\tpush%s %s", getMode(), storage.toString(getMode()));
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		if (storage instanceof RegisterBased) {
			return new RegisterBased[] { (RegisterBased) storage };
		} else {
			return new RegisterBased[] {};
		}
	}
}
