package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class Comment extends AssemblerOperation {

	public Comment(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return "";
	}
}
