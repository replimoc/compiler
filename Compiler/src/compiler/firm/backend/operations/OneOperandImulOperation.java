package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.FixedTwoSourceTwoDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;

public class OneOperandImulOperation extends FixedTwoSourceTwoDestinationOperation {

	public OneOperandImulOperation(String comment, RegisterBased operand, RegisterBased multiplier, RegisterBased resultLow, RegisterBased resultHigh) {
		super(comment, operand, multiplier, resultLow, resultHigh);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul %s", source2);
	}
}
