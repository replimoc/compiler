package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NegOperation extends RegisterOperation {

	public NegOperation(Bit mode, RegisterBased register) {
		super(null, mode, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tneg%s %s", getMode(), getRegister().toString(getMode()));
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] { getRegister() };
	}
}
