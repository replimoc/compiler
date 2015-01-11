package compiler.firm.backend.operations.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class AssemblerBitOperation extends AssemblerOperation {
	private static final Register[] accumulatorRegisters = { Register._10D, Register._11D };

	protected final Bit mode;
	private int accumulatorRegister = 0;

	public AssemblerBitOperation(String comment, Bit mode) {
		super(comment);
		this.mode = mode;
	}

	public Bit getMode() {
		return mode;
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			List<String> result = new ArrayList<>();
			result.add(new Comment("Operation with spill code").toString());
			HashMap<VirtualRegister, Storage> storageMapping = new HashMap<>();
			List<VirtualRegister> storageMappingWrite = new ArrayList<>();

			for (RegisterBased register : getReadRegisters()) {
				if (register.isSpilled()) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					Storage storage = insertSpillcode(virtualRegister, result, true);
					storageMapping.put(virtualRegister, storage);
				}
			}
			for (RegisterBased register : getWriteRegisters()) {
				if (register.isSpilled()) {

					VirtualRegister virtualRegister = (VirtualRegister) register;
					if (!storageMapping.containsKey(register)) {
						Storage storage = insertSpillcode(virtualRegister, result, false);
						storageMapping.put(virtualRegister, storage);
					}
					storageMappingWrite.add(virtualRegister);
				}
			}

			result.add(toString());

			for (Entry<VirtualRegister, Storage> storageMap : storageMapping.entrySet()) {
				VirtualRegister virtualRegister = storageMap.getKey();
				Storage stackPointer = storageMap.getValue();
				if (storageMappingWrite.contains(virtualRegister)) {
					MovOperation spillOperation = new MovOperation(virtualRegister.getMode(), virtualRegister, stackPointer);
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

		if (restore) {
			MovOperation spillOperation = new MovOperation(virtualRegister.getMode(), stackPointer, temporaryRegister);
			result.add(spillOperation.toString());
		}
		virtualRegister.setStorage(temporaryRegister);

		return stackPointer;
	}

	protected Register getTemporaryRegister() {
		if (accumulatorRegister >= accumulatorRegisters.length) {
			throw new RuntimeException("Running out of accumulator registers");
		}
		return accumulatorRegisters[accumulatorRegister++];
	}
}
