package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;

public abstract class RegisterConstantOperation extends AssemblerOperation {

	private final Register register;
	private final Constant constant;

	public RegisterConstantOperation(Register register, Constant constant) {
		this(null, register, constant);
	}

	public RegisterConstantOperation(String comment, Register register, Constant constant) {
		super(comment);
		this.register = register;
		this.constant = constant;
	}

	public Register getRegister() {
		return register;
	}

	public Constant getConstant() {
		return constant;
	}
}
