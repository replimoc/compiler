package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterConstantOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;

public class ShllOperation extends RegisterConstantOperation {

	public ShllOperation(Register register, Constant constant) {
		super(register, constant);
	}

	public ShllOperation(String comment, Register register, Constant constant) {
		super(comment, register, constant);
	}

	@Override
	public String getOperationString() {
		return String.format("\tshll %s, %s", getConstant().toString(Bit.BIT32), getRegister().toString(Bit.BIT32));
	}
}
