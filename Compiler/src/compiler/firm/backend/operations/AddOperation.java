package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class AddOperation extends AssemblerOperation {

	private Register input;
	private Register destination;

	public AddOperation(Register input, Register destinationRegister) {
		this.input = input;
		this.destination = destinationRegister;
	}

	@Override
	public String toString() {
		return String.format("\taddl %s, %s\n", input, destination);
	}
}
