package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.operations.templates.StorageRegisterRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AddOperation extends SourceSourceDestinationOperation {

	public static StorageRegisterRegisterOperationFactory getFactory(final String comment) {
		return new StorageRegisterRegisterOperationFactory() {
			@Override
			public SourceSourceDestinationOperation instantiate(Storage source1, RegisterBased source2, RegisterBased destination) {
				return new AddOperation(comment, source1, source2, destination);
			}
		};
	}

	public AddOperation(Storage source1, RegisterBased source2, RegisterBased destinationRegister) {
		super(null, source1, source2, destinationRegister);
	}

	public AddOperation(String comment, Storage source1, RegisterBased source2, RegisterBased destinationRegister) {
		super(comment, source1, source2, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tadd %s, %s", getSource().toString(), getDestination().toString());
	}
}
