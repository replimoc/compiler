package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public class IdivOperation extends RegisterOperation {

	public IdivOperation(Bit mode, RegisterBased register) {
		super(null, mode, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv%s %s", getMode(), getRegister().toString(getMode()));
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] {
				new VirtualRegister(mode, Register._AX),
				getRegister().getUsedRegister() };
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {
				new VirtualRegister(mode, Register._DX),
				new VirtualRegister(mode, Register._AX)
		};
	}
}
