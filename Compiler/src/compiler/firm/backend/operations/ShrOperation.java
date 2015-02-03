package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.ConstantRegisterRegisterOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public class ShrOperation extends ConstantRegisterRegisterOperation {

	public ShrOperation(String comment, Constant constant, RegisterBased source, RegisterBased destination) {
		super(comment, constant, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tshr %s, %s", getConstant().toString(), destination.toString());
	}
}
