package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

public class RestoreCalleeSavedRegistersOperation extends AssemblerOperation {

	private final CallingConvention callingConvention;

	public RestoreCalleeSavedRegistersOperation(CallingConvention callingConvention) {
		this.callingConvention = callingConvention;
	}

	@Override
	public String[] toStringWithSpillcode() {
		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		String[] result = new String[registers.length];

		for (int i = registers.length - 1, k = 0; i >= 0; i--, k++) {
			result[k] = new PopOperation(registers[i].getRegister(Bit.BIT64)).toString();
		}

		return result;
	}

	@Override
	public String getComment() {
		return "SaveCalleeRegistersOperation";
	}

	@Override
	public String getOperationString() {
		throw new RuntimeException("this should never be called.");
	}

}
