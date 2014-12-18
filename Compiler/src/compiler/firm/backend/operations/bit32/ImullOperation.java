package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class ImullOperation extends StorageRegisterOperation {

	public ImullOperation() {
		this(null);
	}

	public ImullOperation(String comment) {
		super(comment);
	}

	public ImullOperation(Register input, Register destinationRegister) {
		this(null, input, destinationRegister);
	}

	public ImullOperation(String comment, Register input, Register destinationRegister) {
		super(comment, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\timull %s, %s", getStorage().toString32(), getDestination().toString32());
	}
}
