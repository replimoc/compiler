package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.operations.templates.SourceSourceDesinationOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AddOperation extends SourceSourceDestinationOperation {

	public static SourceSourceDesinationOperationFactory getFactory(final String comment) {
		return new SourceSourceDesinationOperationFactory() {
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
		if (isLea()) {
			return String.format("\tlea (%s, %s), %s", source.toString(), source2.toString(), destination.toString());
		} else {
			return String.format("\tadd %s, %s", source.toString(), destination.toString());
		}
	}

	@Override
	protected MovOperation getPreOperation() {
		if (isLea()) {
			return null;
		} else {
			return super.getPreOperation();
		}
	}

	private boolean isLea() {
		return source.getSingleRegister() != destination.getSingleRegister() && source2.getSingleRegister() != destination.getSingleRegister() &&
				source.getSingleRegister() != null && source.getSingleRegister() != null && destination.getSingleRegister() != null;
	}
}
