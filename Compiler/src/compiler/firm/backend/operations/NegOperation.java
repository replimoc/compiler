package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NegOperation extends AssemblerBitOperation {

	private RegisterBased register;

	public NegOperation(Bit mode, RegisterBased register) {
		super(null, mode);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tneg%s %s", getMode(), register.toString(getMode()));
	}
}
