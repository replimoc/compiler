package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class CmpOperation extends StorageRegisterOperation {

	public CmpOperation(Bit mode) {
		this(null, mode);
	}

	public CmpOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public CmpOperation(Bit mode, Register input, Register destinationRegister) {
		super(null, mode, input, destinationRegister);
	}

	public CmpOperation(String comment, Bit mode, Register input, Register destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp %s, %s", getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
