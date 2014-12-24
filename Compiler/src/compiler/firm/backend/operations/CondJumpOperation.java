package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class CondJumpOperation extends AssemblerOperation {

	private String operation;

	private CondJumpOperation(String operation) {
		this.operation = operation;
	}

	public static CondJumpOperation createJumpZero(String label) {
		return new CondJumpOperation("jz " + label);
	}

	public static CondJumpOperation createJump(String label) {
		return new CondJumpOperation("jmp " + label);
	}

	public static CondJumpOperation createJumpLess(String label) {
		return new CondJumpOperation("jl " + label);
	}

	public static CondJumpOperation createJumpLessEqual(String label) {
		return new CondJumpOperation("jle " + label);
	}

	public static CondJumpOperation createJumpGreater(String label) {
		return new CondJumpOperation("jg " + label);
	}

	public static CondJumpOperation createJumpGreaterEqual(String label) {
		return new CondJumpOperation("jge " + label);
	}

	@Override
	protected String getOperationString() {
		return "\t" + operation;
	}

}
