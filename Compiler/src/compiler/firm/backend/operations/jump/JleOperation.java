package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JleOperation extends JumpOperation {

	public JleOperation(LabelOperation label) {
		super(label);
	}

	@Override
	protected String getOperationString() {
		return "\tjle " + getLabelName();
	}

}
