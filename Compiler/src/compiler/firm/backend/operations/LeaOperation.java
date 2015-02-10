package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

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
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(addressStorage.getReadRegisters());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(resultRegister);
	}
}
