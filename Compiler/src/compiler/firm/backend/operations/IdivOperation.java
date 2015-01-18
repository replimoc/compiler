package compiler.firm.backend.operations;

import java.util.Arrays;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

public class IdivOperation extends RegisterOperation {

	public IdivOperation(RegisterBased register) {
		super(null, register);
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv %s", getRegister().toString());
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased[] usedRegister = getRegister().getUsedRegister();
		RegisterBased[] result = Arrays.copyOf(usedRegister, usedRegister.length + 2);
		result[usedRegister.length] = new VirtualRegister(Bit.BIT32, RegisterBundle._DX);
		result[usedRegister.length + 1] = new VirtualRegister(Bit.BIT32, RegisterBundle._AX);
		return result;
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] {
				new VirtualRegister(Bit.BIT32, RegisterBundle._DX),
				new VirtualRegister(Bit.BIT32, RegisterBundle._AX)
		};
	}
}
