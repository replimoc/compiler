package compiler.firm.backend.operations.bit32;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Register;

public class DivOperation extends StorageRegisterOperation {

	public DivOperation() {
		super(null);
	}

	public DivOperation(String comment) {
		super(comment);
	}

	public DivOperation(Register input) {
		super(null, input, null);
	}

	public DivOperation(String comment, Register input) {
		super(comment, input, null);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidivl %s", getStorage().toString(Bit.BIT32));
	}
}
