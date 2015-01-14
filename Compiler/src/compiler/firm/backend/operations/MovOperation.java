package compiler.firm.backend.operations;

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

		Bit sourceMode = source.getMode();
		Bit destinationMode = destination.getMode();
		if (sourceMode != null && destinationMode != null && sourceMode != destinationMode) {
			return String
					.format("\tmovs%s%s %s, %s", sourceMode, destinationMode, source.toString(sourceMode), destination.toString(destinationMode));
		} else {
			return String.format("\tmov%s %s, %s", mode, source.toString(mode), destination.toString(mode));
		}
	}
}
