package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.ConstantRegisterRegisterOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public class ShlOperation extends ConstantRegisterRegisterOperation {

	public ShlOperation(Constant constant, RegisterBased source, RegisterBased destination) {
		super(constant, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tshl %s, %s", getConstant().toString(), getSource().toString());
	}
}
