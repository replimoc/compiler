package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public class CallOperation extends AssemblerOperation {

	private String name;

	public CallOperation(String name) {
		this.name = name;
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
		// TODO: Ask calling convention
		return new RegisterBased[] {
				new VirtualRegister(Register._AX),
				new VirtualRegister(Register._DX),
				new VirtualRegister(Register._CX)
		}; // Return register
	}

}
