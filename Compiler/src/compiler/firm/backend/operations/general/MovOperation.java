package compiler.firm.backend.operations.general;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Storage;

public class MovOperation extends SourceDestinationOperation {

	public MovOperation(Bit mode, Storage source, Storage destination) {
		super(null, mode, source, destination);
	}

	public MovOperation(String comment, Bit mode, Storage source, Storage destination) {
		super(comment, mode, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\tmov%s %s, %s", getMode(), getSource().toString(getMode()), getDestination().toString(getMode()));
	}

}
