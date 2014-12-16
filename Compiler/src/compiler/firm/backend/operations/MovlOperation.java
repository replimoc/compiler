package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class MovlOperation extends AssemblerOperation {

	private final String sourceConstant;
	private final Register source;
	private final String destinationConstant;
	private final Register destination;

	public MovlOperation(int constant, Register destination) {
		this(constant, null, null, destination);
	}

	public MovlOperation(int constant, Register source, Register destination) {
		this(constant, source, null, destination);
	}

	public MovlOperation(Integer sourceConstant, Register source, Integer destinationConstant, Register destination) {
		this.sourceConstant = sourceConstant == null ? null : String.format("%x", sourceConstant);
		this.source = source;
		this.destinationConstant = destinationConstant == null ? null : String.format("%x", destinationConstant);
		this.destination = destination;
	}

	public MovlOperation(Integer sourceConstant, Integer destinationConstant, Register destinationReg) {
		this(sourceConstant, null, destinationConstant, destinationReg);
	}

	@Override
	public String toString() {
		String source = "";
		if (this.source != null) {
			if (sourceConstant == null) {
				source = this.source.toString();
			} else {
				source += sourceConstant + "(" + this.source.toString() + ")";
			}
		} else {
			source = "$0x" + sourceConstant;
		}

		String dest = "";
		if (this.destination != null) {
			if (destinationConstant == null) {
				dest = this.destination.toString();
			} else {
				dest = destinationConstant + "(" + this.destination.toString() + ")";
			}
		} else {
			dest = "$0x" + dest;
		}

		return String.format("\tmovl %s, %s\n", source, dest);
	}

}
