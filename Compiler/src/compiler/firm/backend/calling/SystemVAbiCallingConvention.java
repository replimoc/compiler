package compiler.firm.backend.calling;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.AndOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.StackPointer;

public class SystemVAbiCallingConvention extends CallingConvention {

	@Override
	public AssemblerOperation[] getPrefixOperations() {
		return new AssemblerOperation[] {
				new PushOperation(Bit.BIT64, Register.RSP),
				new PushOperation(Bit.BIT64, new StackPointer(0, Register.RSP)),
				new AndOperation(Bit.BIT64, new Constant(-0x10), Register.RSP)
		};
	}

	@Override
	public AssemblerOperation[] getSuffixOperations() {
		return new AssemblerOperation[] {
				new MovOperation(Bit.BIT64, new StackPointer(8, Register.RSP), Register.RSP)
		};
	}

	@Override
	public Register[] getParameterRegisters() {
		return new Register[] { Register.EDI, Register.ESI, Register.EDX, Register.ECX, Register.R8D, Register.R9D };
	}

	@Override
	public Register getReturnRegister() {
		return Register.EAX;
	}

}
