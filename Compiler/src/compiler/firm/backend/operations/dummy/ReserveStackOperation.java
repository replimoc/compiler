package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class ReserveStackOperation extends AssemblerOperation {

	private AssemblerOperation operation;

	public void setOperation(AssemblerOperation operation) {
		this.operation = operation;
	}

	@Override
	public String getOperationString() {
		return operation == null ? "ReserveStackOperation" : operation.getOperationString();
	}

	@Override
	public String getComment() {
		return operation == null ? "" : operation.getComment();
	}

}
