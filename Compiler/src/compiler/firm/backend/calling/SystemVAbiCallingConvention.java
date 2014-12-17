package compiler.firm.backend.calling;

import compiler.firm.backend.operations.bit64.AndqOperation;
import compiler.firm.backend.operations.bit64.MovqOperation;
import compiler.firm.backend.operations.bit64.PushqOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.StackPointer;

public class SystemVAbiCallingConvention extends CallingConvention {

	@Override
	public AssemblerOperation[] getPrefixOperations() {
		return new AssemblerOperation[] {
				new PushqOperation(Register.RSP),
				new PushqOperation(new StackPointer(0, Register.RSP)),
				new AndqOperation(new Constant(-0x10), Register.RSP)
		};
	}

	@Override
	public AssemblerOperation[] getSuffixOperations() {
		return new AssemblerOperation[] {
				new MovqOperation(new StackPointer(8, Register.RSP), Register.RSP)
		};
	}

	@Override
	public Register[] getParameterRegisters() {
		return new Register[] { Register.EDI, Register.ESI, Register.EDX, Register.ECX };
	}

	@Override
	public Register getReturnRegister() {
		return Register.EAX;
	}

}
