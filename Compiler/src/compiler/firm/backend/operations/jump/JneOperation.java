package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JneOperation extends JumpOperation {

	public JneOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjne " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JeOperation(label);
	}

}
