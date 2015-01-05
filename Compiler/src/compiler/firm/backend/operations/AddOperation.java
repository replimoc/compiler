package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AddOperation extends StorageRegisterOperation {

	public static StorageRegisterOperationFactory getFactory(final String comment, final Bit mode) {
		return new StorageRegisterOperationFactory() {
			@Override
			public StorageRegisterOperation instantiate(Storage input, RegisterBased destination) {
				return new AddOperation(comment, mode, input, destination);
			}
		};
	}

	public AddOperation(Bit mode, Storage input, RegisterBased destinationRegister) {
		super(null, mode, input, destinationRegister);
	}

	public AddOperation(String comment, Bit mode, Storage input, RegisterBased destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tadd%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
