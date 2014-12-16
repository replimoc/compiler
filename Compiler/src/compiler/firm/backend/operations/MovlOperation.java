package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class MovlOperation extends ConstantRegisterOperation {

	private final Register source;

	public MovlOperation(int constant, Register destination) {
		this(constant, null, destination);
	}

	public MovlOperation(int constant, Register source, Register destination) {
		super(constant, destination);
		this.source = source;
	}

	@Override
	public String toString() {
		String source = "";
		if (this.source != null) {
			source = "(" + this.source.toString() + ")";
		}
		return String.format("\tmovl $0x%s%s, %s\n", getConstant(), source, getDestination());
	}

}
