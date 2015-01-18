package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class SubOperation extends StorageRegisterOperation {

	public static StorageRegisterOperationFactory getFactory(final String comment) {
		return new StorageRegisterOperationFactory() {
			@Override
			public StorageRegisterOperation instantiate(Storage input, RegisterBased destination) {
				return new SubOperation(comment, input, destination);
			}
		};
	}

	public SubOperation(Storage input, RegisterBased destinationRegister) {
		super(null, input, destinationRegister);
	}

	public SubOperation(String comment, Storage input, RegisterBased destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsub %s, %s", getStorage().toString(), getDestination().toString());
	}
}
