package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public class PopqOperation extends AssemblerOperation {
	private final Register register;

	public PopqOperation(Register register) {
		this.register = register;
	}

	@Override
	public String toString() {
		return String.format("\tpopq %s\n", register.toString64());
	}
}
