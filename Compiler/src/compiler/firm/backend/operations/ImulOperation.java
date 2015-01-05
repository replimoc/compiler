package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;

public class ImulOperation extends StorageRegisterOperation {

	public ImulOperation(String comment, Bit mode) {
		super(comment, mode);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul%s %s, %s", getMode(), getStorage().toString(getMode()), getDestination().toString(getMode()));
	}
}
