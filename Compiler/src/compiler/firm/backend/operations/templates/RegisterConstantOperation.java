package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;

public abstract class RegisterConstantOperation extends AssemblerBitOperation {

	private final Register register;
	private final Constant constant;

	public RegisterConstantOperation(Bit mode, Register register, Constant constant) {
		this(null, mode, register, constant);
	}

	public RegisterConstantOperation(String comment, Bit mode, Register register, Constant constant) {
		super(comment, mode);
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
