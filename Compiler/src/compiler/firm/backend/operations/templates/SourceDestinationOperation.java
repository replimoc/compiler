package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.Storage;

public abstract class SourceDestinationOperation extends AssemblerOperation {

	private final Storage source;
	private final Storage destination;

	public SourceDestinationOperation(Storage source, Storage destination) {
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
