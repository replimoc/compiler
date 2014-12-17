package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;

public class MovlOperation extends AssemblerOperation {

	private final Integer sourceConstant;
	private final Register sourceRegister;
	private final Integer destinationConstant;
	private final Register destinationRegister;

	public MovlOperation(int constant, Register destination) {
		this(constant, null, null, destination);
	}

	public MovlOperation(Register source, Register destination) {
		this(null, source, null, destination);
	}

	public MovlOperation(int constant, Register source, Register destination) {
		this(constant, source, null, destination);
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

	private String buildName(Integer constant, Register register) {
		String result = "";
		if (register != null) {
			if (constant == null) {
				result = register.toString();
			} else {
				if (constant < 0) {
					result = String.format("-%x(%s)", -constant, register.toString());
				} else {
					result = String.format("%x(%s)", constant, register.toString());
				}
			}
		} else {
			if (constant < 0) {
				result = String.format("$0x-0x%x", constant);
			} else {
				result = String.format("$0x%x", constant);
			}
		}
		return result;
	}

}
