package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;

public class IdivOperation extends RegisterOperation {

	public IdivOperation(Bit mode, RegisterBased register) {
		this(null, mode, register);
	}

	public IdivOperation(String comment, Bit mode, RegisterBased register) {
		super(comment, mode, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv%s %s", getMode(), getRegister().toString(getMode()));
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] { Register._DX, Register._AX, getRegister().getUsedRegister() };
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { Register._DX, Register._AX };
	}
}
