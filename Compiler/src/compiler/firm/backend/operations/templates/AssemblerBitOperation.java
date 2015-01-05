package compiler.firm.backend.operations.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class AssemblerBitOperation extends AssemblerOperation {

	private final Bit mode;
	private int accumulatorRegister = 0;
	private final Register[] accumulatorRegisters = { Register._10D, Register._11D };

	public AssemblerBitOperation(String comment, Bit mode) {
		super(comment);
		this.mode = mode;
	}

	public Bit getMode() {
		return mode;
	}

	@Override
	public final String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			List<String> result = new ArrayList<>();
			result.add(new Comment("Operation with spill code").toString());
			HashMap<VirtualRegister, Storage> storageMapping = new HashMap<>();

			for (RegisterBased register : getReadRegisters()) {
				if (register.isSpilled()) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					Storage storage = insertSpillcode(virtualRegister, result, true);
					storageMapping.put(virtualRegister, storage);
				}
			}
			for (RegisterBased register : getWriteRegisters()) {
				if (register.isSpilled() && !storageMapping.containsKey(register)) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					Storage storage = insertSpillcode(virtualRegister, result, false);
					storageMapping.put(virtualRegister, storage);
				}
			}

			result.add(toString());

			for (Entry<VirtualRegister, Storage> storageMap : storageMapping.entrySet()) {
				VirtualRegister virtualRegister = storageMap.getKey();
				Storage stackPointer = storageMap.getValue();
				if (getClass() != CmpOperation.class) {
					// TODO: Correct mode, see other comment for more details.
					MovOperation spillOperation = new MovOperation(Bit.BIT64, virtualRegister.getRegister(), stackPointer);
					result.add(spillOperation.toString());
				}
				virtualRegister.setStorage(stackPointer);
			}

			String[] resultArray = new String[result.size()];
			result.toArray(resultArray);
			return resultArray;
		} else {
			return new String[] { toString() };
		}
	}

	private Storage insertSpillcode(VirtualRegister virtualRegister, List<String> result, boolean restore) {
		Register temporaryRegister = getTemporaryRegister();
		Storage stackPointer = virtualRegister.getRegister();
		MovOperation spillOperation = new MovOperation(Bit.BIT64, stackPointer, temporaryRegister);
		/*
		 * TODO, correct mode, but it is not possible to use global AssemblerBitOperation mode. Reason is, that this register can also contains
		 * addresses. For example mov %r10d, (%r11) -> read mode is the problem.
		 */
		if (!restore) {
			spillOperation = new MovOperation(Bit.BIT64, new Constant(0), temporaryRegister); // Clear should be only on mode 64
		}
		virtualRegister.setStorage(temporaryRegister);
		if (restore || mode == Bit.BIT8) {
			result.add(spillOperation.toString());
		}
		return stackPointer;
	}

	private Register getTemporaryRegister() {
		if (accumulatorRegister >= accumulatorRegisters.length) {
			throw new RuntimeException("Running out of accumulator registers");
		}
		return accumulatorRegisters[accumulatorRegister++];
	}
}
