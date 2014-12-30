package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JmpOperation extends JumpOperation {

	public JmpOperation(LabelOperation label) {
		super(label);
	}

	@Override
	protected String getOperationString() {
		return "\tjmp " + getLabel();
	}

}
