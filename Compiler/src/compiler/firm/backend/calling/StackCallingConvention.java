package compiler.firm.backend.calling;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public class StackCallingConvention extends CallingConvention {

	@Override
	public AssemblerOperation[] getPrefixOperations() {
		return new AssemblerOperation[] {};
	}

	@Override
	public AssemblerOperation[] getSuffixOperations() {
		return new AssemblerOperation[] {};
	}

	@Override
	public Register[] getParameterRegisters() {
		return new Register[] {};
	}

	@Override
	public Register getReturnRegister() {
		return Register.EAX;
	}

}
