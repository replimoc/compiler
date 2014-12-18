package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Storage;

public class MovlOperation extends SourceDestinationOperation {

	public MovlOperation(Storage source, Storage destination) {
		super(null, source, destination);
	}

	public MovlOperation(String comment, Storage source, Storage destination) {
		super(comment, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tmovl %s, %s", getSource().toString32(), getDestination().toString32());
	}

}
