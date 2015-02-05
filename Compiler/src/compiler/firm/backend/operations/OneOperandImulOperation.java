package compiler.firm.backend.operations;

import java.util.Arrays;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.FixedTwoSourceTwoDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class OneOperandImulOperation extends FixedTwoSourceTwoDestinationOperation {

	public OneOperandImulOperation(String comment, RegisterBased operand, RegisterBased multiplier, RegisterBased resultLow, RegisterBased resultHigh) {
		super(comment, operand, multiplier, resultLow, resultHigh);
	}

	@Override
	public List<String> getOperationString(Bit mode, Storage source2) {
		return Arrays.asList(String.format("\timul%s %s", mode, source2));
	}
}
