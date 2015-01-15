package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;

public abstract class RegisterOperation extends AssemblerBitOperation {

	private RegisterBased register;

	public RegisterOperation(RegisterBased register) {
		this(null, register);
	}

	public RegisterOperation(String comment, RegisterBased register) {
		super(comment);
		this.register = register;
	}

	public RegisterBased getRegister() {
		return register;
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] { this.register };
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { this.register };
	}
}
