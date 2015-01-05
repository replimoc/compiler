package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class SubOperation extends StorageRegisterOperation {

	public static StorageRegisterOperationFactory getFactory(final String comment, final Bit mode) {
		return new StorageRegisterOperationFactory() {
			@Override
			public StorageRegisterOperation instantiate(Storage input, RegisterBased destination) {
				return new SubOperation(comment, mode, input, destination);
			}
		};
	}

	public SubOperation(Bit mode, Storage input, RegisterBased destinationRegister) {
		super(null, mode, input, destinationRegister);
	}

	public SubOperation(String comment, Bit mode, Storage input, RegisterBased destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsub%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
