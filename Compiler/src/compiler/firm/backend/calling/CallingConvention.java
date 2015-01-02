package compiler.firm.backend.calling;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;

public abstract class CallingConvention {
	public static final CallingConvention SYSTEMV_ABI = new SystemVAbiCallingConvention();
	public static final CallingConvention OWN = new StackCallingConvention();

	public abstract AssemblerOperation[] getPrefixOperations();

	public abstract AssemblerOperation[] getSuffixOperations();

	public abstract Register[] getParameterRegisters();

	public abstract Register getReturnRegister();

	public abstract Register[] callerSavedRegisters();

	public abstract Register[] calleeSavedRegisters();
}
