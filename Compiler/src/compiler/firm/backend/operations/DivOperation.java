package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.FixedTwoSourceTwoDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;

public class DivOperation extends FixedTwoSourceTwoDestinationOperation {

	public DivOperation(RegisterBased dividend, RegisterBased divisor, RegisterBased result, RegisterBased remainder) {
		super(null, dividend, divisor, result, remainder);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcltd \n\tidiv %s", source2);
	}
}
