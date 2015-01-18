package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NegOperation extends RegisterOperation {

	public NegOperation(RegisterBased register) {
		super(null, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tneg %s", getRegister().toString());
	}
}
