package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class LabelOperation extends AssemblerOperation {

	private String name;
	private boolean alignment;

	public LabelOperation(String name) {
		this.name = name;
	}

	@Override
	public String getOperationString() {
		return String.format("%s:", name);
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (alignment) {
			return new String[] { ".p2align 4", toString() };
		} else {
			return new String[] { toString() };
		}
	}

	public String getName() {
		return name;
	}

	public void setAlignment(boolean alignment) {
		this.alignment = alignment;
	}
}
