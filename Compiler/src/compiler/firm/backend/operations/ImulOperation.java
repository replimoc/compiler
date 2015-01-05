package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class ImulOperation extends StorageRegisterOperation {

	public static StorageRegisterOperationFactory getFactory(final String comment, final Bit mode) {
		return new StorageRegisterOperationFactory() {
			@Override
			public StorageRegisterOperation instantiate(Storage input, RegisterBased destination) {
				return new ImulOperation(comment, mode, input, destination);
			}
		};
	}

	private ImulOperation(String comment, Bit mode, Storage input, RegisterBased destination) {
		super(comment, mode, input, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}

}
