package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public abstract class ConstantRegisterRegisterOperation extends SourceSourceDestinationOperation {

	public ConstantRegisterRegisterOperation(Constant constant, RegisterBased source, RegisterBased destination) {
		super(null, constant, source, destination);
	}

	public ConstantRegisterRegisterOperation(String comment, Constant constant, RegisterBased source, RegisterBased destination) {
		super(comment, constant, source, destination);
	}

	public Constant getConstant() {
		return (Constant) source;
	}
}
