package compiler.firm.backend.operations;

import java.util.Arrays;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.FixedTwoSourceTwoDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class DivOperation extends FixedTwoSourceTwoDestinationOperation {

	public DivOperation(RegisterBased dividend, RegisterBased divisor, RegisterBased result, RegisterBased remainder) {
		super(null, dividend, divisor, result, remainder);
	}

	@Override
	public List<String> getOperationString(Bit mode, Storage source2) {
		return Arrays.asList(
				"\tcltd",
				String.format("\tidiv%s %s", mode, source2));
	}
}
