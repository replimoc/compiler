package compiler.firm.backend.calling;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

// http://wiki.osdev.org/Calling_Conventions
public class SystemVAbiCallingConvention extends CallingConvention {

	@Override
	public AssemblerOperation[] getPrefixOperations() {
		return new AssemblerOperation[] {
				new PushOperation(Bit.BIT64, Register._SP)
		};
	}

	@Override
	public AssemblerOperation[] getSuffixOperations() {
		return new AssemblerOperation[] {
				new PopOperation(Bit.BIT64, Register._SP)
		};
	}

	@Override
	public Register[] getParameterRegisters() {
		return new Register[] { Register._DI, Register._SI, Register._DX, Register._CX, Register._8D, Register._9D };
	}

	@Override
	public Register getReturnRegister() {
		return Register._AX;
	}

	@Override
	public Register[] callerSavedRegisters() {
		return new Register[] {};
	}

	@Override
	public Register[] calleeSavedRegisters() {
		return new Register[] { Register._BX, Register._SP, Register._BP, Register._12D,
				Register._13D, Register._14D, Register._15D };
	}

}
