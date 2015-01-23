package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmovSignOperation extends SourceSourceDestinationOperation {

	public CmovSignOperation(String comment, Storage source, RegisterBased source2, RegisterBased destination) {
		super(comment, source, source2, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmovs %s, %s", source, destination);
	}
}