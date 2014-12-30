package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class CltdOperation extends AssemblerOperation {

	public CltdOperation() {
		this(null);
	}

	public CltdOperation(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcltd");
	}
}
