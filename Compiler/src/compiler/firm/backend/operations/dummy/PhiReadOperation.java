package compiler.firm.backend.operations.dummy;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
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

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = new PhiGraph(phiRelations).calculateOperations();
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
