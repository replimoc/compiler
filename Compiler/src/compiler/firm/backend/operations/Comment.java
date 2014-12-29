package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;

public class Comment extends AssemblerOperation {

	public Comment(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return "";
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}

}
