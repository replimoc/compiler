package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class IncOperation extends RegisterOperation {

	public IncOperation(String comment, RegisterBased register) {
		super(comment, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tinc %s", super.getRegister().toString());
	}
}
