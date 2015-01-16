package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NotOperation extends RegisterOperation {

	public NotOperation(RegisterBased register) {
		super(null, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tnot %s", getRegister());
	}
}
