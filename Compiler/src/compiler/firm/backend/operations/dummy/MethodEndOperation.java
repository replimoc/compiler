package compiler.firm.backend.operations.dummy;

import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;

public class MethodEndOperation extends MethodStartEndOperation {

	public MethodEndOperation(CallingConvention callingConvention, int stackItemSize) {
		super(callingConvention, stackItemSize);
	}

	@Override
	public String[] toStringWithSpillcode() {

		List<String> result = new LinkedList<String>();

		if (stackOperationSize > 0) {
			result.add(new AddOperation("stack free", new Constant(stackOperationSize), SingleRegister.RSP, SingleRegister.RSP).toString());
		} else {
			result.add(new Comment("no items on stack, skip freeing").toString());
		}

		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		for (int i = registers.length - 1; i >= 0; i--) {
			if (super.isRegisterSaveNeeded(registers[i])) {
				result.add(new PopOperation(registers[i].getRegister(Bit.BIT64)).toString());
			}
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "MethodEndOperation";
	}
}
