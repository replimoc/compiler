package compiler.firm.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

	public AssemblerOperationsBlock(Block block, ArrayList<AssemblerOperation> operations) {
		this.block = block;
		this.operations = operations;
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

		for (int i = operations.size() - 1; i >= 0; i--) {
			AssemblerOperation operation = operations.get(i);

			for (RegisterBased readRegister : operation.getReadRegisters()) {
				uses.add((VirtualRegister) readRegister);
			}
			for (RegisterBased writeRegister : operation.getWriteRegisters()) {
				kills.add((VirtualRegister) writeRegister);
			}
		}
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Live in: " + block + ":  liveOut :");

		for (VirtualRegister register : liveOut) {
			builder.append("VR_" + register.getNum() + ", ");
		}

		builder.append("  liveIn: ");

		for (VirtualRegister register : liveIn) {
			builder.append("VR_" + register.getNum() + ", ");
		}

		return builder.toString();
	}

	public Set<AssemblerOperationsBlock> getPredecessors() {
		return predecessors;
	}
}
