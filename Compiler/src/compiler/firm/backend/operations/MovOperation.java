package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Storage;

public class MovOperation extends SourceDestinationOperation {

	public MovOperation(Storage source, Storage destination) {
		super(null, source, destination);
	}

	public MovOperation(String comment, Storage source, Storage destination) {
		super(comment, source, destination);
	}

	@Override
	public String getOperationString() {
		new Constant(1);
		Bit sourceMode = source.getMode();
		Bit destinationMode = destination.getMode();
		if (sourceMode != null && destinationMode != null && sourceMode != destinationMode) {
			return String.format("\tmovs%s%s %s, %s", sourceMode, destinationMode, source.toString(), destination.toString());
		} else {
			Bit mode;

			if (sourceMode != null) {
				mode = sourceMode;
			} else if (destinationMode != null) {
				mode = destinationMode;
			} else {
				mode = Bit.BIT64;
			}

			return String.format("\tmov%s %s, %s", mode, source.toString(), destination.toString());
		}

	}
}
