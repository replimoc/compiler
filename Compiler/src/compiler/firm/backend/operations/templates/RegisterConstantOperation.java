package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public abstract class RegisterConstantOperation extends AssemblerBitOperation {

	private final RegisterBased register;
	private final Constant constant;

	public RegisterConstantOperation(Bit mode, RegisterBased register, Constant constant) {
		this(null, mode, register, constant);
	}

	public RegisterConstantOperation(String comment, Bit mode, RegisterBased register, Constant constant) {
		super(comment, mode);
		this.register = register;
		this.constant = constant;
	}

	public RegisterBased getRegister() {
		return register;
	}

	public Constant getConstant() {
		return constant;
	}
}
