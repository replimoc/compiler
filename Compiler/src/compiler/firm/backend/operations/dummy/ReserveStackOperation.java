package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;

public class ReserveStackOperation extends AssemblerOperation {

	private SubOperation operation;

	public void setOperation(SubOperation operation) {
		this.operation = operation;
	}

	@Override
	public String getOperationString() {
		return operation.getOperationString();
	}

}
