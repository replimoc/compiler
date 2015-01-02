package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JlOperation extends JumpOperation {

	public JlOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjl " + getLabelName();
	}

}
