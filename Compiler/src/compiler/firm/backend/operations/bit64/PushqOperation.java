package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Storage;

public class PushqOperation extends AssemblerOperation {

	private final Storage storage;

	public PushqOperation(Storage storage) {
		this.storage = storage;
	}

	@Override
	public String toString() {
		return String.format("\tpushq %s\n", storage.toString64());
	}

}
