package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.Storage;

public abstract class SourceDestinationOperation extends AssemblerOperation {

	private final Bit mode;
	private final Storage source;
	private final Storage destination;

	public SourceDestinationOperation(String comment, Bit mode, Storage source, Storage destination) {
		super(comment);
		this.source = source;
		this.destination = destination;
		this.mode = mode;
	}

	public Storage getSource() {
		return source;
	}

	public Storage getDestination() {
		return destination;
	}

	public Bit getMode() {
		return mode;
	}
}
