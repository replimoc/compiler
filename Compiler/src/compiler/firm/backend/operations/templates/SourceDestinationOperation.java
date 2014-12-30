package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Storage;

public abstract class SourceDestinationOperation extends AssemblerBitOperation {

	private final Storage source;
	private final Storage destination;

	public SourceDestinationOperation(String comment, Bit mode, Storage source, Storage destination) {
		super(comment, mode);
		this.source = source;
		this.destination = destination;
	}

	public Storage getSource() {
		return source;
	}

	public Storage getDestination() {
		return destination;
	}
}
