package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JzOperation extends JumpOperation {

	public JzOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjz " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JnzOperation(label);
	}

}
