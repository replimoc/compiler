package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class LeaOperation extends AssemblerBitOperation {
	private final Storage addressStorage;
	private final RegisterBased resultRegister;

	public LeaOperation(Storage addressStorage, RegisterBased resultRegister) {
		super(null);

		this.addressStorage = addressStorage;
		this.resultRegister = resultRegister;
	}

    public LeaOperation(String comment, Storage addressStorage, RegisterBased resultRegister) {
		super(comment);

		this.addressStorage = addressStorage;
		this.resultRegister = resultRegister;
	}

	@Override
	public String getOperationString() {
		return String.format("\tlea %s, %s", addressStorage.toString(), resultRegister.toString());
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
