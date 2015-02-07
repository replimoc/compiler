package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

import firm.Graph;
import firm.nodes.Block;

public class AssemblerProgram {
	private final Graph graph;
	private final Map<Block, AssemblerOperationsBlock> operationsBlocks;
	private final Set<RegisterBundle> usedRegisters = new HashSet<>();

	public AssemblerProgram(Graph graph, HashMap<Block, AssemblerOperationsBlock> operationsBlocks) {
		this.graph = graph;
		this.operationsBlocks = operationsBlocks;
		calculateLiveInAndLiveOut();
	}

	private void calculateLiveInAndLiveOut() {
		final LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>();

		for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
			AssemblerOperationsBlock operationsBlock = entry.getValue();
			if (operationsBlock != null) {
				operationsBlock.calculateTree(operationsBlocks);
				operationsBlock.calculateUsesAndKills();
				workList.add(operationsBlock);
			}
		}

		while (!workList.isEmpty()) {
			AssemblerOperationsBlock operationsBlock = workList.removeLast();
			if (operationsBlock.calculateLiveInAndOut()) {
				workList.addAll(operationsBlock.getPredecessors());
			}
		}

		for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
			SsaRegisterAllocator.debugln(entry.getValue());
		}
	}

	public AssemblerOperationsBlock getOperationsBlock(Block block) {
		return operationsBlocks.get(block);
	}

	public void setDummyOperationsInformation(int stackSize) {
		if (stackSize > 0) {
			stackSize += 0x10;
			stackSize &= -0x10; // Align to 8-byte.
		} else {
			stackSize = 0;
		}

		for (Entry<Block, AssemblerOperationsBlock> curr : operationsBlocks.entrySet()) {
			ArrayList<AssemblerOperation> operations = curr.getValue().getOperations();
			for (AssemblerOperation operation : operations) {
				if (operation instanceof MethodStartEndOperation) {
					MethodStartEndOperation methodStartEndOperation = (MethodStartEndOperation) operation;
					methodStartEndOperation.setStackOperationSize(stackSize);
					methodStartEndOperation.setUsedRegisters(usedRegisters);
				}
			}
		}
	}

	public void addUsedRegister(RegisterBundle freeBundle) {
		usedRegisters.add(freeBundle);
	}

	public Set<RegisterBundle> getUsedRegisters() {
		return usedRegisters;
	}

	public Block getStartBlock() {
		return graph.getStartBlock();
	}

	public Graph getGraph() {
		return graph;
	}
}
