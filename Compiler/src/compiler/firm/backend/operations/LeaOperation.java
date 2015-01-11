package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;

public class LeaOperation extends AssemblerBitOperation {
	private final RegisterBased baseRegister;
	private final RegisterBased factorRegister;
	private final int factor;
	private final RegisterBased resultRegister;

	public LeaOperation(RegisterBased baseRegister, RegisterBased factorRegister, int factor, RegisterBased resultRegister) {
		super(null, Bit.BIT64);

		this.baseRegister = baseRegister;
		this.factorRegister = factorRegister;
		this.factor = factor;
		this.resultRegister = resultRegister;
	}

	@Override
	public String getOperationString() {
		return "\tlea" + Bit.BIT64 + " (" + baseRegister.toString(Bit.BIT64) + ","
				+ factorRegister.toString(Bit.BIT64) + "," + factor + "),"
				+ resultRegister.toString(Bit.BIT64);
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] { baseRegister, factorRegister };
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { resultRegister };
	}
}
