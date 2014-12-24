package compiler.firm.backend.operations.general;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class ImulOperation extends StorageRegisterOperation {

	public ImulOperation(Bit mode) {
		this(null, mode);
	}

	public ImulOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public ImulOperation(Bit mode, Register input, Register destinationRegister) {
		this(null, mode, input, destinationRegister);
	}

	public ImulOperation(String comment, Bit mode, Register input, Register destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
