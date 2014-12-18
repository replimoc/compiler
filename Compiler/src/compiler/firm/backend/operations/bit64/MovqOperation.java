package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Storage;

public class MovqOperation extends SourceDestinationOperation {

	public MovqOperation(Storage source, Storage destination) {
		super(source, destination);
	}

	@Override
	public String toString() {
		return String.format("\tmovq %s, %s\n", getSource().toString64(), getDestination().toString64());
	}

}
