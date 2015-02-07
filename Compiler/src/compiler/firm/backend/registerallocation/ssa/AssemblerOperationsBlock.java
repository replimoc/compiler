package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;

import firm.nodes.Block;
import firm.nodes.Node;

public class AssemblerOperationsBlock {
	private final Block block;
	private final boolean isLoopHead;
	private final ArrayList<AssemblerOperation> operations;
	private final Set<AssemblerOperationsBlock> predecessors = new HashSet<>();
	private final Set<AssemblerOperationsBlock> successors = new HashSet<>();

	private final Map<VirtualRegister, Integer> uses = new HashMap<>();
	private final Set<VirtualRegister> kills = new HashSet<>();
	private final Map<VirtualRegister, Integer> liveIn = new HashMap<>();
	private final Map<VirtualRegister, Integer> liveOut = new HashMap<>();

	private final Map<VirtualRegister, AssemblerOperation> lastUsed = new HashMap<>();

	private final Set<VirtualRegister> wExit = new HashSet<>();

	public AssemblerOperationsBlock(Block block, ArrayList<AssemblerOperation> operations) {
		this.block = block;
		this.operations = operations;

		for (AssemblerOperation operation : operations) {
			operation.setOperationsBlock(this);
		}

		this.isLoopHead = FirmUtils.getLoopTailIfHeader(block) != null;
	}

	public void calculateTree(HashMap<Block, AssemblerOperationsBlock> operationsBlocks) {
		liveOut.clear();

		for (Node pred : block.getPreds()) {
			Node predBlock = pred.getBlock();

			if (predBlock instanceof Block) {
				AssemblerOperationsBlock predOperationsBlock = operationsBlocks.get(predBlock);
				predecessors.add(predOperationsBlock);
				predOperationsBlock.successors.add(this);
			}
		}
	}

	public void calculateUsesAndKills() {
		uses.clear();
		kills.clear();
		lastUsed.clear();

		for (int i = operations.size() - 1; i >= 0; i--) {
			AssemblerOperation operation = operations.get(i);

			for (VirtualRegister readRegister : operation.getVirtualReadRegisters()) {
				uses.put(readRegister, i);

				if (!lastUsed.containsKey(readRegister)) // keep in mind, we go from the last operation to the first one
					lastUsed.put(readRegister, operation);
				readRegister.addUsage(operation);
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				kills.add(writeRegister);
				writeRegister.setDefinition(operation);
			}
		}
	}

	public boolean calculateLiveInAndOut() {
		int oldLiveOutSize = liveOut.size();
		HashMap<VirtualRegister, Integer> oldLiveIn = new HashMap<>(liveIn);

		calculateLiveOut();

		liveIn.clear();

		int blockLength = operations.size();
		for (Entry<VirtualRegister, Integer> liveOutEntry : liveOut.entrySet()) {
			liveIn.put(liveOutEntry.getKey(), liveOutEntry.getValue() + blockLength);
		}
		liveIn.putAll(uses); // this overwrites liveouts that are used internally

		for (VirtualRegister kill : kills) { // remove variables defined in this block
			liveIn.remove(kill);
		}

		return liveOut.size() != oldLiveOutSize || !liveIn.equals(oldLiveIn);
	}

	private void calculateLiveOut() {
		for (AssemblerOperationsBlock succOperationsBlock : successors) {
			mergeNextUseMaps(liveOut, succOperationsBlock.liveIn);
		}
	}

	private static void mergeNextUseMaps(Map<VirtualRegister, Integer> result, Map<VirtualRegister, Integer> toBeMerged) {
		for (Entry<VirtualRegister, Integer> currLiveIn : toBeMerged.entrySet()) {
			Integer existingLiveOut = result.get(currLiveIn.getKey());
			if (existingLiveOut != null) { // if this is also live in somewhere else, take the minimum distance
				result.put(currLiveIn.getKey(), Math.min(existingLiveOut, currLiveIn.getValue()));
			} else {
				result.put(currLiveIn.getKey(), currLiveIn.getValue());
			}
		}
	}

	public Set<VirtualRegister> getLiveIn() {
		return liveIn.keySet();
	}

	public Set<VirtualRegister> getLiveOut() {
		return liveOut.keySet();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(block);
		builder.append("(loop head: " + isLoopHead + "):  \t  liveIn: ");

		for (VirtualRegister register : liveIn.keySet()) {
			builder.append("VR_" + register.getNum() + ", ");
		}

		builder.append("  \t  liveOut: ");

		for (VirtualRegister register : liveOut.keySet()) {
			builder.append("VR_" + register.getNum() + ", ");
		}

		return builder.toString();
	}

	public Set<AssemblerOperationsBlock> getPredecessors() {
		return predecessors;
	}

	public ArrayList<AssemblerOperation> getOperations() {
		return operations;
	}

	public boolean isLastUsage(VirtualRegister register, AssemblerOperation operation) {
		return !liveOut.containsKey(register) && lastUsed.get(register) == operation;
	}

	public int getNextUseDistance(AssemblerOperation operation, VirtualRegister register, boolean exclusive) {
		int i = 0;
		int operationIdx = 0;
		for (AssemblerOperation currOperation : operations) {
			if (currOperation == operation) {
				operationIdx = i;
				if (exclusive) {
					i++;
					continue;
				}
			}
			if (operationIdx >= 0 && currOperation.getVirtualReadRegisters().contains(register)) {
				return i - operationIdx;
			}
			i++;
		}

		if (liveOut.containsKey(register)) {
			return liveOut.get(register) + operations.size() - operationIdx;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public Set<VirtualRegister> calculateWEntry(int availableRegisters) {
		// if (isLoopHead) {
		// calculateWLoopHead(availableRegisters);
		// } else {
		return calculateWNormalBlock(availableRegisters);
		// }
	}

	private Set<VirtualRegister> calculateWNormalBlock(int availableRegisters) {
		Set<VirtualRegister> wEntry = new HashSet<>();

		Map<VirtualRegister, Integer> candidates = new HashMap<>();

		for (AssemblerOperationsBlock pred : predecessors) {
			for (VirtualRegister register : pred.wExit) {
				Integer registerFrequncy = candidates.get(register);
				if (registerFrequncy == null) {
					registerFrequncy = 0;
				}
				candidates.put(register, registerFrequncy + 1);
			}
		}

		int numberOfPredecessors = predecessors.size();
		for (Iterator<Entry<VirtualRegister, Integer>> iterator = candidates.entrySet().iterator(); iterator.hasNext();) {
			Entry<VirtualRegister, Integer> register = iterator.next();
			if (numberOfPredecessors == register.getValue().intValue()) {
				wEntry.add(register.getKey());
				iterator.remove();
			}
		}

		List<VirtualRegister> candidatesList = new ArrayList<>(candidates.keySet());
		Collections.sort(candidatesList, new Comparator<VirtualRegister>() {
			@Override
			public int compare(VirtualRegister o1, VirtualRegister o2) {
				Integer nextUseO1 = liveIn.get(o1);
				Integer nextUseO2 = liveIn.get(o2);
				int iNextUseO1 = nextUseO1 == null ? Integer.MAX_VALUE : nextUseO1;
				int iNextUseO2 = nextUseO2 == null ? Integer.MAX_VALUE : nextUseO2;

				return iNextUseO1 - iNextUseO2;
			}
		});

		boolean debugW = false;
		debugln(debugW, "\n" + block);
		debug(debugW, "\tCandiates: ");
		for (VirtualRegister curr : candidatesList) {
			debug(debugW, "\tVR_" + curr.getNum());
		}
		debugln(debugW, "");

		Iterator<VirtualRegister> iterator = candidatesList.iterator();
		for (int i = wEntry.size(); i < availableRegisters && iterator.hasNext(); i++) {
			wEntry.add(iterator.next());
		}
		debugln(debugW, "\tw: " + wEntry);

		return wEntry;
	}

	private void calculateWLoopHead(int availableRegisters) {
		// TODO Auto-generated method stub

	}

	public void setWExit(Set<VirtualRegister> aliveRegisters) {
		this.wExit.clear();
		this.wExit.addAll(aliveRegisters);
	}

	public void calculateMinAlgorithm(int availableRegisters) {
		boolean debugMinAlgo = true;

		Set<VirtualRegister> aliveRegisters = this.calculateWEntry(availableRegisters);
		Set<VirtualRegister> spilledRegisters = new HashSet<>(liveIn.keySet());
		spilledRegisters.removeAll(aliveRegisters);

		debugln(debugMinAlgo, block);
		debugln(debugMinAlgo, "\twEntry: " + aliveRegisters);
		debugln(debugMinAlgo, "\tspilledEntry: " + spilledRegisters);

		// Min algorithm; see RegisterSpillAndLiveRangeSplittingForSSA page 3
		for (AssemblerOperation operation : operations) {
			Set<VirtualRegister> readRegisters = operation.getVirtualReadRegisters();
			Set<VirtualRegister> reloadRequiringRegisters = new HashSet<VirtualRegister>(readRegisters);
			reloadRequiringRegisters.removeAll(aliveRegisters);
			Set<VirtualRegister> writeRegisters = operation.getVirtualWriteRegisters();

			aliveRegisters.addAll(reloadRequiringRegisters); // add read registers to alive registers
			spilledRegisters.addAll(reloadRequiringRegisters);

			limit(aliveRegisters, spilledRegisters, operation, availableRegisters, false); // limit with read registers
			limit(aliveRegisters, spilledRegisters, operation, availableRegisters - writeRegisters.size(), true); // free space for write registers
			aliveRegisters.addAll(writeRegisters);

			addReloads(reloadRequiringRegisters, operation);
		}

		this.setWExit(aliveRegisters);
	}

	/**
	 * 
	 * @param aliveRegisters
	 * @param spilledRegisters
	 * @param operation
	 * @param limit
	 * @param exclusive
	 *            if true, the live distances are calculated from the operation after the given one.
	 */
	private void limit(Set<VirtualRegister> aliveRegisters, Set<VirtualRegister> spilledRegisters, AssemblerOperation operation, int limit,
			boolean exclusive) {
		ArrayList<Pair<VirtualRegister, Integer>> alivesWithNextUse = getRegistersWithNextUse(aliveRegisters, operation, exclusive);
		Collections.sort(alivesWithNextUse, Pair.<Integer> secondOperator());

		for (int i = alivesWithNextUse.size() - 1; i >= limit; i--) {
			Pair<VirtualRegister, Integer> registerWithNextUse = alivesWithNextUse.get(i);
			if (!spilledRegisters.contains(registerWithNextUse.first) && registerWithNextUse.second != Integer.MAX_VALUE) {
				addSpill(registerWithNextUse.first, operation);
			}

			spilledRegisters.remove(registerWithNextUse.first);
			aliveRegisters.remove(registerWithNextUse.first);
			alivesWithNextUse.remove(i);
		}

		// remove registers that have exceeded their livetime
		for (Iterator<Pair<VirtualRegister, Integer>> iterator = alivesWithNextUse.iterator(); iterator.hasNext();) {
			Pair<VirtualRegister, Integer> curr = iterator.next();
			if (curr.second == Integer.MAX_VALUE) {
				iterator.remove();
				spilledRegisters.remove(curr.first);
				aliveRegisters.remove(curr.first);
			}
		}
	}

	private ArrayList<Pair<VirtualRegister, Integer>> getRegistersWithNextUse(Set<VirtualRegister> aliveRegisters, AssemblerOperation operation,
			boolean exclusive) {
		ArrayList<Pair<VirtualRegister, Integer>> result = new ArrayList<>();
		for (VirtualRegister register : aliveRegisters) {
			result.add(new Pair<>(register, getNextUseDistance(operation, register, exclusive)));
		}
		return result;
	}

	private void addReloads(Set<VirtualRegister> reloadRequiringRegisters, AssemblerOperation operation) {
		for (VirtualRegister register : reloadRequiringRegisters) {
			System.out.println("add reload for " + register + " before " + operation);
		}
	}

	private void addSpill(VirtualRegister register, AssemblerOperation operation) {
		System.out.println("add spill for " + register + " before " + operation);
	}

	private static void debugln(boolean debug, Object o) {
		if (debug)
			System.out.println(o);
	}

	private static void debug(boolean debug, Object o) {
		if (debug)
			System.out.print(o);
	}
}
