package compiler.firm.backend.operations.dummy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;

public class MethodStartOperation extends MethodStartEndOperation {

	private final Set<RegisterBased> writeRegisters = new HashSet<>();

	public MethodStartOperation(CallingConvention callingConvention, int stackItemSize, boolean isMain) {
		super(callingConvention, stackItemSize, isMain);
	}

	@Override
	public String[] toStringWithSpillcode() {

		List<String> result = new LinkedList<String>();

		if (!isMain) {
			RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
			for (int i = 0; i < registers.length; i++) {
				if (super.isRegisterSaveNeeded(registers[i])) {
					result.add(new PushOperation(registers[i].getRegister(Bit.BIT64)).toString());
				}
			}
		}

		if (stackOperationSize > 0) {
			result.add(new SubOperation("stack reservation", new Constant(stackOperationSize), SingleRegister.RSP, SingleRegister.RSP).toString());
		} else {
			result.add(new Comment("no items on stack, skip reservation").toString());
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "MethodStartOperation" + writeRegisters;
	}

	public void addWriteRegister(RegisterBased storage) {
		writeRegisters.add(storage);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return writeRegisters;
	}
}
