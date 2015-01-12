package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class IncOperation extends RegisterOperation {

	public IncOperation(String comment, Bit mode, RegisterBased register) {
		super(comment, mode, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tinc %s", super.getRegister().toString(getMode()));
	}
}
