package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class LeaOperation extends AssemblerBitOperation {
	private final Storage addressStorage;
	private final RegisterBased resultRegister;

	public LeaOperation(Storage addressStorage, RegisterBased resultRegister) {
		super(null, Bit.BIT64);

		this.addressStorage = addressStorage;
		this.resultRegister = resultRegister;
	}

	@Override
	public String getOperationString() {
		return String.format("\tlea%s %s, %s", Bit.BIT64, addressStorage.toString(Bit.BIT64), resultRegister.toString(Bit.BIT64));
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return addressStorage.getUsedRegister();
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { resultRegister };
	}
}
