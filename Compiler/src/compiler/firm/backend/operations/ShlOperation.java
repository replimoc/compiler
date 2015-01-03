package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterConstantOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;

public class ShlOperation extends RegisterConstantOperation {

	public ShlOperation(Bit mode, Register register, Constant constant) {
		super(mode, register, constant);
	}

	@Override
	public String getOperationString() {
		return String.format("\tshl%s %s, %s", getMode(), getConstant().toString(getMode()), getRegister().toString(getMode()));
	}
}
