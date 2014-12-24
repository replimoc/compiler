package compiler.firm.backend.operations.general;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;

public class SubOperation extends StorageRegisterOperation {

	public SubOperation(Bit mode) {
		super(null, mode);
	}

	public SubOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	public SubOperation(Bit mode, Storage input, Register destinationRegister) {
		super(null, mode, input, destinationRegister);
	}

	public SubOperation(String comment, Bit mode, Storage input, Register destinationRegister) {
		super(comment, mode, input, destinationRegister);
	}

	@Override
	public String getOperationString() {
		return String.format("\tsub%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
