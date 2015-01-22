package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class AndOperation extends SourceSourceDestinationOperation {

	public AndOperation(Storage source1, RegisterBased source2, RegisterBased destination) {
		super(null, source1, source2, destination);
	}
	public AndOperation(String comment, Storage storage, RegisterBased destination) {
		super(comment, storage, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tand %s, %s", getSource().toString(), getDestination().toString());
	}
}
