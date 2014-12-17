package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;

public class PopqOperation extends AssemblerOperation {
	private final Register register;

	public PopqOperation(Register register) {
		this.register = register;
	}

	@Override
	public String toString() {
		return String.format("\tpopq %s\n", register);
	}
}
