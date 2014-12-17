package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Storage;

public class PushqOperation extends AssemblerOperation {

	private final Storage storage;

	public PushqOperation(Storage storage) {
		this.storage = storage;
	}

	@Override
	public String toString() {
		return String.format("\tpushq %s\n", storage);
	}

}
