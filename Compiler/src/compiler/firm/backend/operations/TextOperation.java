package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;

public class TextOperation extends AssemblerOperation {

	public TextOperation() {
		super(null);
	}

	@Override
	public String getOperationString() {
		return "\t.text";
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}
}
