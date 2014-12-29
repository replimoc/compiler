package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;

public class P2AlignOperation extends AssemblerOperation {

	@Override
	public String getOperationString() {
		return "\t.p2align 4,,15";
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}
}
