package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

import firm.nodes.Block;
import firm.nodes.Node;

public class AssemblerOperationsBlock {
	private final Block block;
	private final ArrayList<AssemblerOperation> operations;
	private final Set<AssemblerOperationsBlock> predecessors = new HashSet<>();
	private final Set<AssemblerOperationsBlock> successors = new HashSet<>();

	private final Set<VirtualRegister> uses = new HashSet<>();
	private final Set<VirtualRegister> kills = new HashSet<>();
	private final Set<VirtualRegister> liveIn = new HashSet<>();
	private final Set<VirtualRegister> liveOut = new HashSet<>();

	private final HashMap<VirtualRegister, AssemblerOperation> lastUsed = new HashMap<>();

	public AssemblerOperationsBlock(Block block, ArrayList<AssemblerOperation> operations) {
		this.block = block;
		this.operations = operations;

		for (AssemblerOperation operation : operations) {
			operation.setOperationsBlock(this);
		}
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

			for (RegisterBased readRegisterBased : operation.getReadRegisters()) {
				VirtualRegister readRegister = (VirtualRegister) readRegisterBased;
				uses.add(readRegister);

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

	public Set<VirtualRegister> calculatePreallocatedRegisters() {
		Set<VirtualRegister> preallocatedRegisters = new HashSet<>();

		for (VirtualRegister use : uses) {
			if (use.getRegister() != null) {
				preallocatedRegisters.add(use);
			}
		}
		for (VirtualRegister kill : kills) {
			if (kill.getRegister() != null) {
				preallocatedRegisters.add(kill);
			}
		}

		return preallocatedRegisters;
	}

	public boolean calculateLiveInAndOut() {
		int oldLiveInSize = liveOut.size();

		calculateLiveOut();

		liveIn.clear();
		liveIn.addAll(uses);
		liveIn.addAll(liveOut);
		liveIn.removeAll(kills);

		return liveIn.size() != oldLiveInSize;
	}

	private void calculateLiveOut() {
		for (AssemblerOperationsBlock succOperationsBlock : successors) {
			liveOut.addAll(succOperationsBlock.liveIn);
		}
	}

	public Set<VirtualRegister> getLiveIn() {
		return liveIn;
	}

	public Set<VirtualRegister> getLiveOut() {
		return liveOut;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(block + ": \t liveOut: ");

		for (VirtualRegister register : liveOut) {
			builder.append("VR_" + register.getNum() + ", ");
		}

		builder.append("     liveIn: ");

		for (VirtualRegister register : liveIn) {
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
		return !liveOut.contains(register) && lastUsed.get(register) == operation;
	}

	public boolean dominates(AssemblerOperationsBlock otherBlock) {
		return FirmUtils.blockPostdominates(block, otherBlock.block);
	}

	public boolean strictlyDominates(AssemblerOperation operation1, AssemblerOperation operation2) {
		for (AssemblerOperation operation : operations) { // loop over operations to find dominating operation
			if (operation == operation2) {
				return false;
			} else if (operation == operation1) {
				return true;
			}
		}
		return false;
	}

}
