package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class RetOperation extends AssemblerOperation {

	public RetOperation(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return "\tret";
	}
}
