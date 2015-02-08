package compiler.firm.backend.operations.dummy.phi;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.TransferGraphSolver;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;

public class PhiReadOperation extends AssemblerBitOperation {
	private final List<Pair<Storage, RegisterBased>> phiRelations;

	public PhiReadOperation(List<Pair<Storage, RegisterBased>> phiRelations) {
		super("Handle phis of follower block");
		this.phiRelations = phiRelations;
	}

	@Override
	public String getOperationString() {
		return "\t# phi read ";
	}

	public void addPhiRelation(Storage source, VirtualRegister destination) {
		phiRelations.add(new Pair<Storage, RegisterBased>(source, destination));
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = TransferGraphSolver.calculateOperations(phiRelations);
		String[] resultString = new String[result.size()];
		result.toArray(resultString);
		return resultString;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		Set<RegisterBased> registers = new HashSet<>();
		for (Pair<Storage, RegisterBased> phiStorage : phiRelations) {
			registers.addAll(phiStorage.getFirst().getReadRegisters());
		}
		return registers;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}
}
