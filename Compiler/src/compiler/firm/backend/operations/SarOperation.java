package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.ConstantRegisterRegisterOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public class SarOperation extends ConstantRegisterRegisterOperation {

	public SarOperation(Constant constant, RegisterBased source, RegisterBased destination) {
		super(constant, source, destination);
	}

	public SarOperation(String comment, Constant constant, RegisterBased source, RegisterBased destination) {
		super(comment, constant, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsar %s, %s", getConstant(), getDestination());
	}
}
