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
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;
import compiler.utils.Utils;

public class ProxyPhiOperation extends AssemblerBitOperation {
	private final LinkedList<Pair<Storage, PhiOperation>> phiStorages;

	public ProxyPhiOperation(LinkedList<Pair<Storage, PhiOperation>> phiStorages) {
		super("Handle phis of current block");

		this.phiStorages = phiStorages;

		Set<VirtualRegister> phiRegister = new HashSet<>();
		for (Pair<Storage, PhiOperation> phiInfo : phiStorages) {
			phiRegister.add(phiInfo.getSecond().getRegister());
		}

		// TODO: Optimize this loop to mark only conflicting, and not all other
		for (VirtualRegister register : phiRegister) {
			register.addInteference(phiRegister);
		}
	}

	@Override
	public String getOperationString() {
		String comment = "";
		for (Pair<Storage, PhiOperation> phi : phiStorages) {
			comment += phi.getSecond().getComment() + ", ";
		}
		return "\t\t\t# " + comment;
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<RegisterBundle> conflictingRegisters = new ArrayList<>();
		for (Pair<Storage, PhiOperation> phiStorage : phiStorages) {
			RegisterBundle source = phiStorage.getFirst().getRegisterBundle();
			if (source != phiStorage.getSecond().getRegister().getRegisterBundle()) {
				conflictingRegisters.add(source);
			}
		}

		List<String> result = new ArrayList<>();

		while (phiStorages.size() > 0) {
			Pair<Storage, PhiOperation> first = phiStorages.pop();
			RegisterBundle sourceBundle = first.getFirst().getRegisterBundle();
			VirtualRegister destination = first.getSecond().getRegister();
			RegisterBundle destinationBundle = destination.getRegisterBundle();
			if (destinationBundle != null && conflictingRegisters.contains(destinationBundle)) {
				phiStorages.addLast(first);
			} else {
				conflictingRegisters.remove(sourceBundle);
				result.addAll(Arrays.asList(new MovOperation(first.getSecond().getComment(), first.getFirst(), destination).toStringWithSpillcode()));
			}
		}

		String[] resultString = new String[result.size()];
		result.toArray(resultString);
		return resultString;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		Set<RegisterBased> registers = new HashSet<>();
		for (Pair<Storage, PhiOperation> phiStorage : phiStorages) {
			RegisterBased[] usedRegisters = phiStorage.getFirst().getUsedRegister();
			if (usedRegisters != null)
				registers.addAll(Utils.unionSet(usedRegisters));
		}
		return registers;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

}
