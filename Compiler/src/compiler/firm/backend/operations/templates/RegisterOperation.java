package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;

public abstract class RegisterOperation extends AssemblerBitOperation {

	private RegisterBased register;

	public RegisterOperation(Bit mode, RegisterBased register) {
		this(null, mode, register);
	}

	public RegisterOperation(String comment, Bit mode, RegisterBased register) {
		super(comment, mode);
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
