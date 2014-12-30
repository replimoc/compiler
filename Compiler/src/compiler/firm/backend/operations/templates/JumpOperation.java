package compiler.firm.backend.operations.templates;

import compiler.firm.backend.operations.LabelOperation;

public abstract class JumpOperation extends AssemblerOperation {

	private final LabelOperation label;

	public JumpOperation(LabelOperation label) {
		this.label = label;
	}

	public String getLabel() {
		return label.getName();
	}
}
