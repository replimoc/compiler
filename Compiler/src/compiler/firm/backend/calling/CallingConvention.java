package compiler.firm.backend.calling;

import compiler.firm.backend.storage.RegisterBundle;

public abstract class CallingConvention {
	public static final CallingConvention SYSTEMV_ABI = new SystemVAbiCallingConvention();
	public static final CallingConvention OWN = new StackCallingConvention();

	public abstract RegisterBundle[] getParameterRegisters();

	public abstract RegisterBundle getReturnRegister();

	public abstract RegisterBundle[] callerSavedRegisters();

	public abstract RegisterBundle[] calleeSavedRegisters();
}
