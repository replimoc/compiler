package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;

public class LabelOperation extends AssemblerOperation {

	private String name;

	public LabelOperation(String name) {
		this.name = name;
	}

	@Override
	public String getOperationString() {
		return String.format("%s:", name);
	}

	public String getName() {
		return name;
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}
}
