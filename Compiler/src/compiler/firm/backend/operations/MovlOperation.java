package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Storage;

public class MovlOperation extends SourceDestinationOperation {

	public MovlOperation(Storage source, Storage destination) {
		super(source, destination);
	}

	@Override
	public String toString() {
		return String.format("\tmovl %s, %s\n", getSource(), getDestination());
	}

}
