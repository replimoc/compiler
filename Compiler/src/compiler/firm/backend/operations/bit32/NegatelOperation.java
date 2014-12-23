package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public class NegatelOperation extends AssemblerOperation {

	private Register register;

	public NegatelOperation(Register register) {
		super(null);
		this.register = register;
	}

	@Override
	public String getOperationString() {
		return String.format("\tnegl %s", register.toString32());
	}
}
