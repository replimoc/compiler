package compiler.firm.backend.operations.general;

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
