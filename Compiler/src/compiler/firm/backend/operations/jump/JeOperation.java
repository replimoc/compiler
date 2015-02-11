package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JeOperation extends JumpOperation {

	public JeOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tje " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JneOperation(label);
	}

}
