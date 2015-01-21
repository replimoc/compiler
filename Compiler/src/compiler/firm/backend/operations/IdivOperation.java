package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class IdivOperation extends RegisterOperation {

	private final VirtualRegister result = new VirtualRegister(Bit.BIT32, RegisterBundle._AX);
	private final VirtualRegister remainder = new VirtualRegister(Bit.BIT32, RegisterBundle._DX);

	public IdivOperation(RegisterBased register) {
		super(null, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv %s", register.toString());
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(register.getUsedRegister(), new RegisterBased[] { result, remainder });
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.<RegisterBased> unionSet(result, remainder);
	}

	public VirtualRegister getResult() {
		return result;
	}

	public VirtualRegister getRemainder() {
		return remainder;
	}
}
