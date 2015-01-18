package compiler.firm.backend.operations.dummy;

import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;

public class MethodEndOperation extends MethodStartEndOperation {

	public MethodEndOperation(CallingConvention callingConvention) {
		super(callingConvention);
	}

	@Override
	public String[] toStringWithSpillcode() {

		List<String> result = new LinkedList<String>();

		if (stackOperationSize > 0) {
			result.add(new AddOperation("stack free", new Constant(stackOperationSize), SingleRegister.RSP).toString());
		} else {
			result.add(new Comment("no items on stack, skip freeing").toString());
		}

		if (!isMain) { // no need to restore the callee registers, they haven't been stored anyway.
			RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
			for (int i = registers.length - 1; i >= 0; i--) {
				result.add(new PopOperation(registers[i].getRegister(Bit.BIT64)).toString());
			}
		}

		result.add(new MovOperation(SingleRegister.RBP, SingleRegister.RSP).toString());
		result.add(new PopOperation(SingleRegister.RBP).toString());

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "SaveCalleeRegistersOperation";
	}
}
