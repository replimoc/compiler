package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class ConstantRegisterRegisterOperation extends SourceSourceDestinationOperation {

	public ConstantRegisterRegisterOperation(Constant constant, RegisterBased source, RegisterBased destination) {
		this(null, constant, source, destination);
	}

	public ConstantRegisterRegisterOperation(String comment, Constant constant, RegisterBased source, RegisterBased destination) {
		super(comment, constant, source, destination);

		if (source instanceof VirtualRegister && destination instanceof VirtualRegister) {
			((VirtualRegister) destination).addPreferedRegister((VirtualRegister) source);
		}
	}

	public Constant getConstant() {
		return (Constant) source;
	}
}
