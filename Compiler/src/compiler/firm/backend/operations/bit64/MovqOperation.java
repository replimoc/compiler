package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Storage;

public class MovqOperation extends SourceDestinationOperation {

	public MovqOperation(Storage source, Storage destination) {
		super(null, source, destination);
	}

	public MovqOperation(String comment, Storage source, Storage destination) {
		super(comment, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tmovq %s, %s", getSource().toString64(), getDestination().toString64());
	}

}
