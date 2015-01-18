package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AddOperation extends StorageRegisterOperation {

	public static StorageRegisterOperationFactory getFactory(final String comment) {
		return new StorageRegisterOperationFactory() {
			@Override
			public StorageRegisterOperation instantiate(Storage input, RegisterBased destination) {
				return new AddOperation(comment, input, destination);
			}
		};
	}

	public AddOperation(Storage input, RegisterBased destinationRegister) {
		super(null, input, destinationRegister);
	}

	public AddOperation(String comment, Storage input, RegisterBased destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tadd %s, %s", getStorage().toString(), getDestination().toString());
	}
}
