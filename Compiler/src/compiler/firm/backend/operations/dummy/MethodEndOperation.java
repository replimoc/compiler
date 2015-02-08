package compiler.firm.backend.operations.dummy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class MethodEndOperation extends MethodStartEndOperation {

	private final RegisterBased returnValue;

	public MethodEndOperation(String comment, CallingConvention callingConvention, int stackItemSize, RegisterBased returnValue, boolean isMain) {
		super(comment, callingConvention, stackItemSize, isMain);
		this.returnValue = returnValue;

		if (returnValue != null && returnValue instanceof VirtualRegister) {
			((VirtualRegister) returnValue).addPreferedRegister(new VirtualRegister(returnValue.getMode(), callingConvention.getReturnRegister()));
		}
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = new LinkedList<String>();

		if (returnValue != null)
			result.add(new MovOperation(returnValue, callingConvention.getReturnRegister().getRegister(returnValue.getMode())).toString());

		if (stackOperationSize > 0) {
			result.add(new AddOperation("stack free", new Constant(stackOperationSize), SingleRegister.RSP, SingleRegister.RSP).toString());
		} else {
			result.add(new Comment("no items on stack, skip freeing").toString());
		}

		if (!isMain) {
			RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
			for (int i = registers.length - 1; i >= 0; i--) {
				if (super.isRegisterSaveNeeded(registers[i])) {
					result.add(new PopOperation(registers[i].getRegister(Bit.BIT64)).toString());
				}
			}
		}

		result.add("\tret");

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "MethodEndOperation";
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		if (returnValue != null) {
			return Utils.unionSet(returnValue);
		} else {
			return Collections.emptySet();
		}
	}
}
