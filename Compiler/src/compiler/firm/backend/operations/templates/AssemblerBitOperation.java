package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;

public abstract class AssemblerBitOperation extends AssemblerOperation {

	private final Bit mode;

	public AssemblerBitOperation(Bit mode) {
		this.mode = mode;
	}

	public AssemblerBitOperation(String comment, Bit mode) {
		super(comment);
		this.mode = mode;
	}

	public Bit getMode() {
		return mode;
	}
}
