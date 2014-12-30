package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;

public class PopOperation extends AssemblerBitOperation {
	private final Register register;

	public PopOperation(Bit mode, Register register) {
		this(null, mode, register);
	}

	public PopOperation(String comment, Bit mode, Register register) {
		super(comment, mode);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tpop%s %s", getMode(), register.toString(getMode()));
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] {};
	}
}
