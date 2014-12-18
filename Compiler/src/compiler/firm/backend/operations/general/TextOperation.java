package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class TextOperation extends AssemblerOperation {

	public TextOperation() {
		super(null);
	}

	@Override
	public String getOperationString() {
		return "\t.text";
	}

}
