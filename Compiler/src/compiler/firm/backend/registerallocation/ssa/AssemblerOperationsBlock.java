package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.ReloadOperation;
import compiler.firm.backend.operations.SpillOperation;
import compiler.firm.backend.operations.dummy.PhiReadOperation;
import compiler.firm.backend.operations.dummy.PhiWriteOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;
import compiler.utils.Utils;

import firm.nodes.Block;
import firm.nodes.Node;

public class AssemblerOperationsBlock {
	private final Block block;
	private final boolean isLoopHead;
	private ArrayList<AssemblerOperation> operations;
	private final AssemblerOperation conditionOrJump;
	private final PhiReadOperation phiRead;
	private final PhiWriteOperation phiWrite;

	private final HashMap<AssemblerOperation, List<AssemblerOperation>> additionalOperations = new HashMap<>();

	private final Set<AssemblerOperationsBlock> predecessors = new HashSet<>();
	private final Set<AssemblerOperationsBlock> successors = new HashSet<>();

	private final Map<VirtualRegister, Integer> uses = new HashMap<>();
	private final Set<VirtualRegister> kills = new HashSet<>();
	private final Map<VirtualRegister, Integer> liveIn = new HashMap<>();
	private final Map<VirtualRegister, Integer> liveOut = new HashMap<>();

	private final Map<VirtualRegister, AssemblerOperation> lastUsed = new HashMap<>();

	private final Set<VirtualRegister> wExit = new HashSet<>();
	private final Set<VirtualRegister> sExit = new HashSet<>();

	private final Set<AssemblerOperationsBlock> dominanceFrontier = new HashSet<>();

	public AssemblerOperationsBlock(Block block, ArrayList<AssemblerOperation> operations) {
		this.block = block;
		this.operations = operations;

		AssemblerOperation conditionOrJump = null;
		PhiReadOperation phiRead = null;
		PhiWriteOperation phiWrite = null;

		for (AssemblerOperation operation : operations) {
			operation.setOperationsBlock(this);

			if (conditionOrJump == null) {
				if (operation instanceof JumpOperation) {
					conditionOrJump = operation;
				} else if (operation instanceof CmpOperation) {
					conditionOrJump = operation;
				}
			} else if (operation instanceof PhiReadOperation) {
				phiRead = (PhiReadOperation) operation;
			} else if (operation instanceof PhiWriteOperation) {
				phiWrite = (PhiWriteOperation) operation;
			}
		}
		this.conditionOrJump = conditionOrJump;
		this.phiRead = phiRead;
		this.phiWrite = phiWrite;

		this.isLoopHead = FirmUtils.getLoopTailIfHeader(block) != null;
	}

	public void calculateTree(Map<Block, AssemblerOperationsBlock> operationsBlocks) {
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

	public boolean isLoopHead() {
		return isLoopHead;
	}

	public Set<AssemblerOperationsBlock> getSuccessors() {
		return successors;
	}

	public Block getBlock() {
		return block;
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

	// private void calculateWLoopHead(int availableRegisters) {
	// // TODO Auto-generated method stub
	// }

	private Set<VirtualRegister> calculateSEntry(Set<VirtualRegister> wEntry) {
		Set<VirtualRegister> sEntry = new HashSet<>();

		for (AssemblerOperationsBlock predecessor : predecessors) {
			for (VirtualRegister predSExit : predecessor.sExit) {
				if (wEntry.contains(predSExit)) {
					sEntry.add(predSExit);
				}
			}
		}

		return sEntry;
	}

	private void setWExit(Set<VirtualRegister> aliveRegisters) {
		this.wExit.clear();
		this.wExit.addAll(aliveRegisters);
	}

	private void setSExit(Set<VirtualRegister> spilledRegisters) {
		this.sExit.clear();
		this.sExit.addAll(spilledRegisters);
	}

	public void executeMinAlgorithm(Map<VirtualRegister, List<ReloadOperation>> insertedReloads, int availableRegisters,
			StackInfoSupplier stackInfoSupplier) {
		boolean debugMinAlgo = false;

		Set<VirtualRegister> aliveRegisters = this.calculateWEntry(availableRegisters);
		Set<VirtualRegister> spilledRegisters = this.calculateSEntry(aliveRegisters);

		insertCupplingCode(insertedReloads, stackInfoSupplier, aliveRegisters, spilledRegisters);

		debugln(debugMinAlgo, block);
		debugln(debugMinAlgo, "\twEntry: " + aliveRegisters);
		debugln(debugMinAlgo, "\tsEntry: " + spilledRegisters);

		// Min algorithm; see RegisterSpillAndLiveRangeSplittingForSSA page 3
		for (AssemblerOperation operation : operations) {
			Set<VirtualRegister> readRegisters = operation.getVirtualReadRegisters();
			Set<VirtualRegister> reloadRequiringRegisters = new HashSet<VirtualRegister>(readRegisters);
			reloadRequiringRegisters.removeAll(aliveRegisters);
			Set<VirtualRegister> writeRegisters = operation.getVirtualWriteRegisters();

			aliveRegisters.addAll(reloadRequiringRegisters); // add read registers to alive registers
			spilledRegisters.addAll(reloadRequiringRegisters);

			// limit the pressure with read registers loaded
			limit(stackInfoSupplier, aliveRegisters, spilledRegisters, operation, availableRegisters, false);
			// free space for write registers
			limit(stackInfoSupplier, aliveRegisters, spilledRegisters, operation, availableRegisters - writeRegisters.size(), true);
			aliveRegisters.addAll(writeRegisters); // add the written registers

			addReloads(insertedReloads, stackInfoSupplier, reloadRequiringRegisters, operation);
		}

		mergeAdditionalOperations();
		this.setWExit(aliveRegisters);
		this.setSExit(spilledRegisters);

		debugln(debugMinAlgo, "\tsExit: " + sExit);
		debugln(debugMinAlgo, "\twExit: " + wExit);
	}

	private void insertCupplingCode(Map<VirtualRegister, List<ReloadOperation>> insertedReloads, StackInfoSupplier stackInfoSupplier,
			Set<VirtualRegister> wEntry, Set<VirtualRegister> sEntry) {
		for (AssemblerOperationsBlock predecessor : predecessors) {
			Set<VirtualRegister> reloads = new HashSet<>(wEntry);
			reloads.removeAll(predecessor.wExit);

			Set<VirtualRegister> spills = new HashSet<>(sEntry);
			spills.removeAll(predecessor.sExit);
			Utils.cutSet(spills, predecessor.wExit);

			List<AssemblerOperation> couplingOperations = new LinkedList<>();
			for (VirtualRegister spilledRegister : spills) {
				couplingOperations.add(createSpillOperation(stackInfoSupplier, spilledRegister));
			}
			for (VirtualRegister reloadedRegister : reloads) {
				ReloadOperation reloadOperation = createReloadOperation(stackInfoSupplier, reloadedRegister);
				couplingOperations.add(reloadOperation);
				Utils.appendToKey(insertedReloads, reloadedRegister, reloadOperation);
			}
			predecessor.additionalOperations.put(predecessor.conditionOrJump, couplingOperations);
			predecessor.mergeAdditionalOperations();
		}
	}

	/**
	 * 
	 * @param stackInfoSupplier
	 * @param aliveRegisters
	 * @param spilledRegisters
	 * @param operation
	 * @param limit
	 * @param exclusive
	 *            if true, the live distances are calculated from the operation after the given one.
	 */
	private void limit(StackInfoSupplier stackInfoSupplier, Set<VirtualRegister> aliveRegisters, Set<VirtualRegister> spilledRegisters,
			AssemblerOperation operation, int limit, boolean exclusive) {

		ArrayList<Pair<VirtualRegister, Integer>> alivesWithNextUse = getRegistersWithNextUse(aliveRegisters, operation, exclusive);
		Collections.sort(alivesWithNextUse, Pair.<Integer> secondOperator());

		for (int i = alivesWithNextUse.size() - 1; i >= limit; i--) {
			Pair<VirtualRegister, Integer> registerWithNextUse = alivesWithNextUse.get(i);
			if (!spilledRegisters.contains(registerWithNextUse.first) && registerWithNextUse.second != Integer.MAX_VALUE) {
				addSpill(stackInfoSupplier, registerWithNextUse.first, operation);
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

	private void addReloads(Map<VirtualRegister, List<ReloadOperation>> insertedReloads, StackInfoSupplier stackInfoSupplier,
			Set<VirtualRegister> reloadedRegisters, AssemblerOperation operation) {

		for (VirtualRegister reloadedRegister : reloadedRegisters) {
			ReloadOperation reloadOperation = createReloadOperation(stackInfoSupplier, reloadedRegister);
			Utils.appendToKey(insertedReloads, reloadedRegister, reloadOperation);
			Utils.appendToKey(additionalOperations, operation, reloadOperation);
		}
	}

	private ReloadOperation createReloadOperation(StackInfoSupplier stackInfoSupplier, VirtualRegister register) {
		return new ReloadOperation(stackInfoSupplier.getStackLocation(register), register);
	}

	private void addSpill(StackInfoSupplier stackInfoSupplier, VirtualRegister register, AssemblerOperation operation) {
		SpillOperation spillOperation = createSpillOperation(stackInfoSupplier, register);
		Utils.appendToKey(additionalOperations, operation, spillOperation);
	}

	private SpillOperation createSpillOperation(StackInfoSupplier stackInfoSupplier, VirtualRegister register) {
		return new SpillOperation(register, stackInfoSupplier.allocateStackLocation(register));
	}

	private void mergeAdditionalOperations() {
		if (additionalOperations.isEmpty()) {
			return;
		}

		ArrayList<AssemblerOperation> oldOperations = operations;
		operations = new ArrayList<>(oldOperations.size());
		for (AssemblerOperation operation : oldOperations) {
			List<AssemblerOperation> additionals = additionalOperations.get(operation);
			if (additionals != null) {
				operations.addAll(additionals);
			}
			operations.add(operation);
		}
		additionalOperations.clear();
	}

	private static void debugln(boolean debug, Object o) {
		if (debug)
			System.out.println(o);
	}

	private static void debug(boolean debug, Object o) {
		if (debug)
			System.out.print(o);
	}

	public void calculateDominanceFrontier() {
		dominanceFrontier.clear();
		for (AssemblerOperationsBlock succ : successors) {
			if (FirmUtils.blockDominates(block, succ.block)) {
				for (AssemblerOperationsBlock succFrontierBlock : succ.dominanceFrontier) {
					if (!FirmUtils.blockDominates(block, succFrontierBlock.block)) {
						dominanceFrontier.add(succFrontierBlock);
					}
				}
			} else {
				dominanceFrontier.add(succ);
			}
		}
		System.out.println("dominance frontier of " + block + ": " + dominanceFrontier);
	}
}
