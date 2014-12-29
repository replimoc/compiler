package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;

public abstract class AssemblerOperation {

	private final String comment;

	public AssemblerOperation() {
		this.comment = null;
	}

	public AssemblerOperation(String comment) {
		this.comment = comment;
	}

	@Override
	public final String toString() {
		String operationString = getOperationString();
		return comment == null ? operationString : operationString + "\t# " + comment;
	}

	protected abstract String getOperationString();

	public abstract RegisterBased[] getUsedRegisters();
}
