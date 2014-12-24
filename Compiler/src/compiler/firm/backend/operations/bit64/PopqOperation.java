package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public class PopqOperation extends AssemblerOperation {
	private final Register register;

	public PopqOperation(Register register) {
		super(null);
		this.register = register;
	}

	public PopqOperation(String comment, Register register) {
		super(comment);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tpopq %s", register.toString(Bit.BIT64));
	}
}
