package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class MovqOperation extends AssemblerOperation {

	private final Register source;
	private final Register destination;
	private final int offset;

	public MovqOperation(Register source, Register destination, int offset) {
		this.source = source;
		this.destination = destination;
		this.offset = offset;
	}

	@Override
	public String toString() {
		return String.format("\tmovq %d(%s), %s\n", offset, source, destination);
	}

}
