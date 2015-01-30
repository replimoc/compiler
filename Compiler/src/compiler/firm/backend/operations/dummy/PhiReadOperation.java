package compiler.firm.backend.operations.dummy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Pair;

public class PhiReadOperation extends AssemblerBitOperation {
	private final LinkedList<Pair<Storage, RegisterBased>> phiStorages;

	public PhiReadOperation(LinkedList<Pair<Storage, RegisterBased>> phiStorages) {
		super("Handle phis of current block");
		this.phiStorages = phiStorages;
	}

	@Override
	public String getOperationString() {
		return "\t# phi read ";
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<RegisterBundle> conflictingRegisters = new ArrayList<>();
		for (Pair<Storage, RegisterBased> phiStorage : phiStorages) {
			RegisterBundle source = phiStorage.getFirst().getRegisterBundle();
			if (source != phiStorage.getSecond().getRegisterBundle()) {
				conflictingRegisters.add(source);
			}
		}

		List<String> result = new ArrayList<>();

		while (phiStorages.size() > 0) {
			Pair<Storage, RegisterBased> first = phiStorages.pop();
			RegisterBundle sourceBundle = first.getFirst().getRegisterBundle();
			RegisterBased destination = first.getSecond();
			RegisterBundle destinationBundle = destination.getRegisterBundle();
			if (destinationBundle != null && conflictingRegisters.contains(destinationBundle)) {
				phiStorages.addLast(first);
			} else {
				conflictingRegisters.remove(sourceBundle);
				result.addAll(Arrays.asList(new MovOperation("phi", first.getFirst(), destination).toStringWithSpillcode()));
			}
		}

		String[] resultString = new String[result.size()];
		result.toArray(resultString);
		return resultString;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		Set<RegisterBased> registers = new HashSet<>();
		for (Pair<Storage, RegisterBased> phiStorage : phiStorages) {
			registers.addAll(phiStorage.getFirst().getReadRegisters());
		}
		return registers;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

}
