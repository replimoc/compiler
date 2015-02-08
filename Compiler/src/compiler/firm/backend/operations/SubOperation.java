package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.operations.templates.SourceSourceDesinationOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class SubOperation extends SourceSourceDestinationOperation {

	public static SourceSourceDesinationOperationFactory getFactory(final String comment) {
		return new SourceSourceDesinationOperationFactory() {
			@Override
			public SourceSourceDestinationOperation instantiate(Storage source, RegisterBased source2, RegisterBased destination) {
				return new SubOperation(comment, source, source2, destination);
			}
		};
	}

	private boolean negateResult;

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

	@Override
	protected MovOperation getPreOperation() {
		// TODO: Detect equal StackPointers.
		if (source.getSingleRegister() == destination.getSingleRegister()) {
			swapSources();
			negateResult = true;
			return null;
		}

		return super.getPreOperation();
	}

	@Override
	protected AssemblerOperation getPostOperation() {
		if (negateResult) {
			return new NegOperation(destination, destination);
		} else {
			return null;
		}
	}

}
