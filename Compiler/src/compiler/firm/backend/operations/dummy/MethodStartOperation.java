package compiler.firm.backend.operations.dummy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;

public class MethodStartOperation extends MethodStartEndOperation {

	private final List<Pair<SingleRegister, RegisterBased>> registerMoves = new LinkedList<>();

	public MethodStartOperation(String comment, CallingConvention callingConvention, int stackItemSize, boolean isMain) {
		super(comment, callingConvention, stackItemSize, isMain);
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

		for (Pair<SingleRegister, RegisterBased> curr : registerMoves) {
			result.add(new MovOperation(curr.first, curr.second).toString());
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getComment() {
		return "MethodStartOperation " + registerMoves;
	}

	public void addWriteRegister(SingleRegister source, RegisterBased target) {
		registerMoves.add(new Pair<SingleRegister, RegisterBased>(source, target));
		if (target instanceof VirtualRegister) {
			((VirtualRegister) target).addPreferedRegister(new VirtualRegister(source));
		}
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		Set<RegisterBased> writeRegisters = new HashSet<>();
		for (Pair<SingleRegister, RegisterBased> curr : registerMoves) {
			writeRegisters.add(curr.second);
		}
		return writeRegisters;
	}
}
