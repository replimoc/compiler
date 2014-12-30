package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class RetOperation extends AssemblerOperation {

	@Override
	public String getOperationString() {
		return "\tret";
	}
}
