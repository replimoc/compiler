package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterConstantOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;

public class ShlqOperation extends RegisterConstantOperation {

	public ShlqOperation(Register register, Constant constant) {
		super(register, constant);
	}

	public ShlqOperation(String comment, Register register, Constant constant) {
		super(comment, register, constant);
	}

	@Override
	public String getOperationString() {
		return String.format("\tshlq %s, %s", getConstant().toString(Bit.BIT64), getRegister().toString(Bit.BIT64));
	}
}
