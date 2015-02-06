package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

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

	private final HashMap<VirtualRegister, AssemblerOperation> lastUsed = new HashMap<>();

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
}
