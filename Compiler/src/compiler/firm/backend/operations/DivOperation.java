package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

// TODO: This is only a register class, why StorageRegister is used?!
public class DivOperation extends StorageRegisterOperation {

	public DivOperation(Bit mode, Register input) {
		super(null, mode, input, null);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv%s %s", getMode(), getStorage().toString(getMode()));
	}
}
