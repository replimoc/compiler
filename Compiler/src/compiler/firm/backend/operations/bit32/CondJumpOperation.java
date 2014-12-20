package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class CondJumpOperation extends AssemblerOperation {

	private String operation;

	private CondJumpOperation(String operation) {
		this.operation = operation;
	}

	public static CondJumpOperation createJumpZero(String label) {
		return new CondJumpOperation("jz " + label);
	}

	public static CondJumpOperation createJumpNoZero(String label) {
		return new CondJumpOperation("jnz " + label);
	}

	public static CondJumpOperation createJump(String label) {
		return new CondJumpOperation("jmp " + label);
	}

	@Override
	protected String getOperationString() {
		return "\t" + operation;
	}

}
