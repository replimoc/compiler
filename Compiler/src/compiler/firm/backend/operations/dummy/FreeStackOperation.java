package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;

public class FreeStackOperation extends AssemblerOperation {

	private AddOperation operation;

	public void setOperation(AddOperation operation) {
		this.operation = operation;
	}

	@Override
	protected String getOperationString() {
		return operation.getOperationString();
	}

}
