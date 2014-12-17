package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Storage;

public class MovqOperation extends SourceDestinationOperation {

	public MovqOperation(Storage source, Storage destination) {
		super(source, destination);
	}

	@Override
	public String toString() {
		return String.format("\tmovq %s, %s\n", getSource(), getDestination());
	}

}
