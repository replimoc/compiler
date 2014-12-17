package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public class AddlOperation extends AssemblerOperation {

	private Register input;
	private Register destination;

	public AddlOperation(Register input, Register destinationRegister) {
		this.input = input;
		this.destination = destinationRegister;
	}

	@Override
	public String toString() {
		return String.format("\taddl %s, %s\n", input, destination);
	}
}
