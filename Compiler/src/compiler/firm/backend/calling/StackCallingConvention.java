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
		return Register._AX;
	}

	@Override
	public Register[] callerSavedRegisters() {
		return new Register[] { Register._13D, Register._14D, Register._15D };
	}

	@Override
	public Register[] calleeSavedRegisters() {
		return new Register[] { Register._BX, Register._SP, Register._BP };
	}

}
