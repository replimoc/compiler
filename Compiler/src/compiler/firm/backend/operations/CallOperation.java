package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;

public class CallOperation extends AssemblerOperation {

	private final String name;
	private final CallingConvention callingConvention;

	public CallOperation(String name, CallingConvention callingConvention) {
		this.name = name;
		this.callingConvention = callingConvention;
	}

	@Override
	public String getOperationString() {
		return String.format("\tcall %s", name);
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return getWriteRegisters();
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { callingConvention.getReturnRegister().getRegister(Bit.BIT64) };
	}
}
