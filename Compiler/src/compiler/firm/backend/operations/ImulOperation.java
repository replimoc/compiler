package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.operations.templates.SourceSourceDesinationOperationFactory;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class ImulOperation extends SourceSourceDestinationOperation {

	public static SourceSourceDesinationOperationFactory getFactory(final String comment) {
		return new SourceSourceDesinationOperationFactory() {
			@Override
			public SourceSourceDestinationOperation instantiate(Storage source1, RegisterBased source2, RegisterBased destination) {
				return new ImulOperation(comment, source1, source2, destination);
			}
		};
	}

	public ImulOperation(String comment, Storage source1, RegisterBased source2, RegisterBased destination) {
		super(comment, source1, source2, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul %s, %s", source.toString(), destination.toString());
	}
}
