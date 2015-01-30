package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.TripleSourceDoubleDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

public class IdivOperation extends TripleSourceDoubleDestinationOperation {

	public IdivOperation(VirtualRegister rax, VirtualRegister rdx, RegisterBased divisor) {
		super(null, rax, rdx, divisor, new VirtualRegister(Bit.BIT32, RegisterBundle._AX), new VirtualRegister(Bit.BIT32, RegisterBundle._DX));
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv %s", getDivisor());
	}

	public RegisterBased getDivisor() {
		return source3;
	}

	public VirtualRegister getResult() {
		return destination1;
	}

	public VirtualRegister getRemainder() {
		return destination2;
	}
}
