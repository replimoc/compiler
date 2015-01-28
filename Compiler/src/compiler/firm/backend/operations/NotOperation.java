package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NotOperation extends SourceDestinationOperation {

	public NotOperation(RegisterBased source, RegisterBased destination) {
		super(source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tnot %s", destination);
	}
}
