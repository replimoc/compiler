package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

public class SaveCalleeSavedRegistersOperation extends AssemblerOperation {

	private final CallingConvention callingConvention;

	public SaveCalleeSavedRegistersOperation(CallingConvention callingConvention) {
		this.callingConvention = callingConvention;
	}

	@Override
	public String[] toStringWithSpillcode() {
		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		String[] result = new String[registers.length];

		for (int i = 0; i < registers.length; i++) {
			result[i] = new PushOperation(Bit.BIT64, registers[i].getRegister(Bit.BIT64)).toString();
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
