package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class CltdOperation extends AssemblerOperation {

	public CltdOperation() {
		super(null);
	}

	public CltdOperation(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcltd");
	}
}
