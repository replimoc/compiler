package compiler.firm.backend.operations.jump;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

public class JleOperation extends JumpOperation {

	public JleOperation(LabelOperation label) {
		super(label);
	}

	@Override
	public String getOperationString() {
		return "\tjle " + getLabelName();
	}

	@Override
	public JumpOperation invert(LabelOperation label) {
		return new JgOperation(label);
	}

}
