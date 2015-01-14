package compiler.firm.backend.calling;

import compiler.firm.backend.storage.RegisterBundle;

public class StackCallingConvention extends CallingConvention {

	@Override
	public RegisterBundle[] getParameterRegisters() {
		return new RegisterBundle[] {};
	}

	@Override
	public RegisterBundle getReturnRegister() {
		return RegisterBundle._AX;
	}

	@Override
	public RegisterBundle[] callerSavedRegisters() {
		return new RegisterBundle[] { RegisterBundle._BX, RegisterBundle._DI, RegisterBundle._SI, RegisterBundle._DX, RegisterBundle._CX,
				RegisterBundle._8D, RegisterBundle._9D, RegisterBundle._10D, RegisterBundle._11D,
				RegisterBundle._12D, RegisterBundle._13D, RegisterBundle._14D, RegisterBundle._15D };
	}

	@Override
	public RegisterBundle[] calleeSavedRegisters() {
		return new RegisterBundle[] { RegisterBundle._BX, RegisterBundle._SP, RegisterBundle._BP };
	}

}
