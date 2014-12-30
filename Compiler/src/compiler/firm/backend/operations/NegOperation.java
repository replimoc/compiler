package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.Register;

public class NegOperation extends AssemblerBitOperation {

	private Register register;

	public NegOperation(Bit mode, Register register) {
		super(null, mode);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tneg%s %s", getMode(), register.toString(getMode()));
	}
}
