package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.operations.templates.StorageRegisterRegisterOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class SubOperation extends SourceSourceDestinationOperation {

	public static StorageRegisterRegisterOperationFactory getFactory(final String comment) {
		return new StorageRegisterRegisterOperationFactory() {
			@Override
			public SourceSourceDestinationOperation instantiate(Storage source, RegisterBased source2, RegisterBased destination) {
				return new SubOperation(comment, source, source2, destination);
			}
		};
	}

	public SubOperation(Storage source, RegisterBased source2, RegisterBased destination) {
		super(null, source, source2, destination);
	}

	public SubOperation(String comment, Storage input, RegisterBased source2, RegisterBased destination) {
		super(comment, input, source2, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsub %s, %s", getSource().toString(), getDestination().toString());
	}
}
