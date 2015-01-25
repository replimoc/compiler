package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;

public class NegOperation extends SourceDestinationOperation {

	public NegOperation(RegisterBased source, RegisterBased destination) {
		super(source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tneg %s", destination.toString());
	}
}
