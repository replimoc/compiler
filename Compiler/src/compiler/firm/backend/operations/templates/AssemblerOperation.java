package compiler.firm.backend.operations.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class AssemblerOperation {

	private final String comment;
	private final Register[] accumulatorRegisters = { Register._10D, Register._11D };

	public AssemblerOperation() {
		this.comment = null;
	}

	public AssemblerOperation(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public final String toString() {
		String operationString = getOperationString();
		return getComment() == null ? operationString : operationString + "\t# " + getComment();
	}

	public final String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			List<String> result = new ArrayList<>();
			result.add(new Comment("Operation with spill code").toString());
			HashMap<VirtualRegister, Storage> storageMapping = new HashMap<>();
			int accumulatorRegister = 0;

			// TODO: Optimize: Use only used variables.
			for (RegisterBased register : getUsedRegisters()) {
				if (register.getClass() == VirtualRegister.class &&
						((VirtualRegister) register).isSpilled()) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					if (accumulatorRegister >= accumulatorRegisters.length) {
						throw new RuntimeException("Running out of accumulator registers");
					}
					Register temporaryRegister = accumulatorRegisters[accumulatorRegister++];
					Storage stackPointer = virtualRegister.getRegister();
					MovOperation spillOperation = new MovOperation(Bit.BIT64, stackPointer, temporaryRegister); // TODO, correct mode
					storageMapping.put(virtualRegister, stackPointer);
					virtualRegister.setStorage(temporaryRegister);
					result.add(spillOperation.toString());
				}
			}

			result.add(toString());

			for (Entry<VirtualRegister, Storage> storageMap : storageMapping.entrySet()) {
				VirtualRegister virtualRegister = storageMap.getKey();
				Storage stackPointer = storageMap.getValue();
				if (getClass() != CmpOperation.class) {
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

	public abstract String getOperationString();

	public RegisterBased[] getReadRegisters() {
		return new RegisterBased[] {};
	}

	public RegisterBased[] getUsedRegisters() {
		return new RegisterBased[] {};
	}

	public boolean hasSpilledRegisters() {
		for (RegisterBased register : getUsedRegisters()) {
			if (register.getClass() == VirtualRegister.class && ((VirtualRegister) register).isSpilled()) {
				return true;
			}
		}
		return false;
	}
}
