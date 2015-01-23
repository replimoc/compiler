package compiler.firm.backend.calling;

import compiler.firm.backend.storage.RegisterBundle;

// http://wiki.osdev.org/Calling_Conventions
public class SystemVAbiCallingConvention extends CallingConvention {

	@Override
	public RegisterBundle[] getParameterRegisters() {
		return new RegisterBundle[] { RegisterBundle._DI, RegisterBundle._SI, RegisterBundle._DX, RegisterBundle._CX, RegisterBundle._8D,
				RegisterBundle._9D };
	}

	@Override
	public RegisterBundle getReturnRegister() {
		return RegisterBundle._AX;
	}

	@Override
	public RegisterBundle[] callerSavedRegisters() {
		return new RegisterBundle[] { // Not RAX - because restore is not possible - conflict with return type!
		RegisterBundle._DI, RegisterBundle._SI, RegisterBundle._DX, RegisterBundle._CX,
				RegisterBundle._8D, RegisterBundle._9D, RegisterBundle._10D, RegisterBundle._11D
		};
	}

	@Override
	public RegisterBundle[] calleeSavedRegisters() {
		return new RegisterBundle[] { RegisterBundle._SP, RegisterBundle._BP, RegisterBundle._BX, RegisterBundle._12D,
				RegisterBundle._13D, RegisterBundle._14D, RegisterBundle._15D };
	}

}
