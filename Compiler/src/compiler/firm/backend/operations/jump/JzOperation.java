package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JzOperation extends JumpOperation {

	public JzOperation(LabelOperation label) {
		super(label);
	}

	@Override
	protected String getOperationString() {
		return "\tjz " + getLabel() + "\n";
	}

}
