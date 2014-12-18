package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public abstract class TwoRegOperandsOperation extends AssemblerOperation {

	private Storage input;
	private Register destination;

	public TwoRegOperandsOperation(String comment) {
		super(comment);
	}

	public TwoRegOperandsOperation(String comment, Storage input, Register destination) {
		super(comment);
		initialize(input, destination);
	}

	public Storage getInputRegister() {
		return input;
	}

	public Register getDestinationRegister() {
		return destination;
	}

	public void initialize(Storage input, Register destination) {
		this.input = input;
		this.destination = destination;
	}
}
