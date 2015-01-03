package compiler.firm.backend.operations;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public class CallOperation extends AssemblerOperation {

	private String name;
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
		Register[] writeRegisters = callingConvention.callerSavedRegisters();
		RegisterBased[] virtualRegisters = new RegisterBased[writeRegisters.length + 1];

		int i = 0;
		for (Register register : writeRegisters) {
			virtualRegisters[i++] = new VirtualRegister(register);
		}
		virtualRegisters[i] = new VirtualRegister(callingConvention.getReturnRegister());
		return virtualRegisters;
	}
}
