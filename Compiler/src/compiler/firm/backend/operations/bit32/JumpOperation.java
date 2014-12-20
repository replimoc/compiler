package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class JumpOperation extends AssemblerOperation {

	private String operation;

	private JumpOperation(String operation) {
		this.operation = operation;
	}

	public static JumpOperation createJumpZero(String label) {
		return new JumpOperation("jz " + label);
	}

	public static AssemblerOperation createJumpNoZero(String label) {
		return new JumpOperation("jnz " + label);
	}

	@Override
	protected String getOperationString() {
		return "\t" + operation;
	}

}
