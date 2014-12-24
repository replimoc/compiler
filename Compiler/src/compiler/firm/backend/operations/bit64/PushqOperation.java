package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Storage;

public class PushqOperation extends AssemblerOperation {

	private final Storage storage;

	public PushqOperation(Storage storage) {
		super(null);
		this.storage = storage;
	}

	public PushqOperation(String comment, Storage storage) {
		super(comment);
		this.storage = storage;
	}

	@Override
	public String getOperationString() {
		return String.format("\tpushq %s", storage.toString(Bit.BIT64));
	}

}
