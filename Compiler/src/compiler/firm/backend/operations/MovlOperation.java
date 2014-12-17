package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Storage;

public class MovlOperation extends AssemblerOperation {

	private final Storage source;
	private final Storage destination;

	public MovlOperation(Storage source, Storage destination) {
		this.source = source;
		this.destination = destination;
	}

	@Override
	public String toString() {
		return String.format("\tmovl %s, %s\n", source, destination);
	}

}
