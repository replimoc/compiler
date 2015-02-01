package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class LabelOperation extends AssemblerOperation {

	private String name;
	private boolean align;

	public LabelOperation(String name) {
		this.name = name;
	}

	public LabelOperation(String name, boolean align) {
		this.name = name;
		this.align = align;
	}

	@Override
	public String getOperationString() {
		return String.format("%s:", name);
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (align) {
			return new String[] { ".p2align 4", toString() };
		} else {
			return new String[] { toString() };
		}
	}

	public String getName() {
		return name;
	}

	public void setAlign(boolean align) {
		this.align = align;
	}
}
