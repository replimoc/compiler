package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class DecOperation extends RegisterOperation {

	public DecOperation(String comment, Bit mode, RegisterBased register) {
		super(comment, mode, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tdec %s", super.getRegister().toString(getMode()));
	}
}
