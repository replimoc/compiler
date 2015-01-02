package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;

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
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] { Register._AX }; // Return register
	}

}
