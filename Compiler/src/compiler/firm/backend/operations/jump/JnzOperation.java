package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JnzOperation extends JumpOperation {

	public JnzOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjnz " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JzOperation(label);
	}

}
