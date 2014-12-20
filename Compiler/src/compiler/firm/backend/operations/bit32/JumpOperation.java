package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class JumpOperation extends AssemblerOperation {

	private String operation;

	private JumpOperation(String operation) {
		this.operation = operation;
	}

	public static JumpOperation createJump(String label) {
		return new JumpOperation("jmp " + label);
	}

	@Override
	protected String getOperationString() {
		return "\t" + operation;
	}

}
