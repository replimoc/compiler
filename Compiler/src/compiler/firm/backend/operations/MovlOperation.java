package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class MovlOperation extends ConstantRegisterOperation {

	public MovlOperation(int constant, Register destination) {
		super(constant, destination);
	}

	@Override
	public String toString() {
		return String.format("\tmovl $0x%s, %s\n", getConstant(), getDestination());
	}

}
