package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;

public class CltdOperation extends AssemblerOperation {

	public CltdOperation() {
		this(null);
	}

	public CltdOperation(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcltd");
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] { Register._AX };
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { Register._DX };
	}

}
