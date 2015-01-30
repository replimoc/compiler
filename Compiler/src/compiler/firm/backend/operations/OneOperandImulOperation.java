package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.DoubleSourceDoubleDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

public class OneOperandImulOperation extends DoubleSourceDoubleDestinationOperation {

	public OneOperandImulOperation(String comment, RegisterBased rax, RegisterBased multiplier) {
		super(comment, rax, multiplier, new VirtualRegister(Bit.BIT32, RegisterBundle._AX), new VirtualRegister(Bit.BIT32, RegisterBundle._DX));
	}

	@Override
	public String getOperationString() {
		return String.format("\timul %s", getMultiplier());
	}

	public RegisterBased getMultiplier() {
		return source2;
	}

	public VirtualRegister getResultLow() {
		return destination1;
	}

	public VirtualRegister getResultHigh() {
		return destination2;
	}

}
