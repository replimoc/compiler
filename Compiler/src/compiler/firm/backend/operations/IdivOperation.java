package compiler.firm.backend.operations;

import java.util.Arrays;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBundle;
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
		RegisterBased[] usedRegister = getRegister().getUsedRegister();
		RegisterBased[] result = Arrays.copyOf(usedRegister, usedRegister.length + 2);
		result[usedRegister.length] = new VirtualRegister(mode, RegisterBundle._DX.getRegister(mode));
		result[usedRegister.length + 1] = new VirtualRegister(mode, RegisterBundle._AX.getRegister(mode));
		return result;
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {
				new VirtualRegister(mode, RegisterBundle._DX.getRegister(mode)),
				new VirtualRegister(mode, RegisterBundle._AX.getRegister(mode))
		};
	}
}
