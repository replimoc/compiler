package compiler.firm.backend.operations.dummy;

import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;

public class MethodStartOperation extends MethodStartEndOperation {

	public MethodStartOperation(CallingConvention callingConvention, int stackItemSize) {
		super(callingConvention, stackItemSize);
	}

	@Override
	public String[] toStringWithSpillcode() {

		List<String> result = new LinkedList<String>();

		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		for (int i = 0; i < registers.length; i++) {
			if (super.isRegisterSaveNeeded(registers[i])) {
				result.add(new PushOperation(Bit.BIT64, registers[i].getRegister(Bit.BIT64)).toString());
			}
		}

		if (stackOperationSize > 0) {
			result.add(new SubOperation("stack reservation", new Constant(stackOperationSize), SingleRegister.RSP).toString());
		} else {
			result.add(new Comment("no items on stack, skip reservation").toString());
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "MethodStartOperation";
	}
}
