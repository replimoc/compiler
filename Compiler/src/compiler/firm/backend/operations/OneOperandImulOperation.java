package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class OneOperandImulOperation extends RegisterOperation {

	private final VirtualRegister resultLow = new VirtualRegister(Bit.BIT32, RegisterBundle._AX);
	private final VirtualRegister resultHigh = new VirtualRegister(Bit.BIT32, RegisterBundle._DX);

	public OneOperandImulOperation(String comment, RegisterBased register) {
		super(comment, register);
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(register, this.resultLow, this.resultHigh);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.<RegisterBased> unionSet(resultLow, resultHigh);
	}

	@Override
	public String getOperationString() {
		return String.format("\timul %s", getRegister().toString());
	}

	public VirtualRegister getResultLow() {
		return resultLow;
	}

	public VirtualRegister getResultHigh() {
		return resultHigh;
	}

}
