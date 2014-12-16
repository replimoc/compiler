package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class MovlOperation extends AssemblerOperation {

	private final int sourceConstant;
	private final Register sourceRegister;
	private final int destinationConstant;
	private final Register destinationRegister;

	public MovlOperation(int constant, Register destination) {
		this(constant, null, 0, destination);
	}

	public MovlOperation(Register source, Register destination) {
		this(0, source, 0, destination);
	}

	public MovlOperation(int constant, Register source, Register destination) {
		this(constant, source, 0, destination);
	}

	public MovlOperation(Integer sourceConstant, Integer destinationConstant, Register destinationReg) {
		this(sourceConstant, null, destinationConstant, destinationReg);
	}

	public MovlOperation(Integer sourceConstant, Register source, Integer destinationConstant, Register destination) {
		this.sourceConstant = sourceConstant;
		this.sourceRegister = source;
		this.destinationConstant = destinationConstant;
		this.destinationRegister = destination;
	}

	@Override
	public String toString() {
		String source = buildName(this.sourceConstant, this.sourceRegister);
		String destination = buildName(this.destinationConstant, this.destinationRegister);

		return String.format("\tmovl %s, %s\n", source, destination);
	}

	private String buildName(int constant, Register register) {
		String result = "";
		if (register != null) {
			if (constant == 0) {
				result = register.toString();
			} else {
				result = String.format("%x(%s)", constant, register.toString());
			}
		} else {
			result = String.format("$0x%x", constant);
		}
		return result;
	}

}
