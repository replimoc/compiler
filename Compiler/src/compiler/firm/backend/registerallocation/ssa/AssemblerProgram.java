package compiler.firm.backend.registerallocation.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.storage.RegisterBundle;

import firm.BlockWalker;
import firm.Graph;
import firm.nodes.Block;

public class AssemblerProgram {
	private final HashMap<Block, AssemblerOperationsBlock> operationsBlocks;

	public AssemblerProgram(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		this.operationsBlocks = createOperationsBlocks(operationsOfBlocks);
		calculateLiveInAndLiveOut(graph);
	}

	private void calculateLiveInAndLiveOut(Graph graph) {
		final LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				AssemblerOperationsBlock operationsBlock = operationsBlocks.get(block);
				if (operationsBlock != null) {
					operationsBlock.calculateTree(operationsBlocks);
					operationsBlock.calculateUsesAndKills();
					workList.add(operationsBlock);
				}
			}
		});

		while (!workList.isEmpty()) {
			AssemblerOperationsBlock operationsBlock = workList.removeLast();
			if (operationsBlock.calculateLiveInAndOut()) {
				workList.addAll(operationsBlock.getPredecessors());
			}
		}

		if (SsaRegisterAllocator.DEBUG) {
			for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
				System.out.println(entry.getValue());
			}
		}
	}

	private static HashMap<Block, AssemblerOperationsBlock> createOperationsBlocks(HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		HashMap<Block, AssemblerOperationsBlock> operationsBlocks = new HashMap<>();

		for (Entry<Block, ArrayList<AssemblerOperation>> entry : operationsOfBlocks.entrySet()) {
			operationsBlocks.put(entry.getKey(), new AssemblerOperationsBlock(entry.getKey(), entry.getValue()));
		}

		return operationsBlocks;
	}

	public AssemblerOperationsBlock getOperationsBlock(Block block) {
		return operationsBlocks.get(block);
	}

	public void setDummyOperationsInformation(Set<RegisterBundle> usedRegisters, int stackSize, boolean isMainMethod,
			RegisterAllocationPolicy policy) {
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

					if (isMainMethod) { // if it is the main, no registers need to be saved
						methodStartEndOperation.setUsedRegisters(new HashSet<RegisterBundle>());
					} else {
						methodStartEndOperation.setUsedRegisters(usedRegisters);
					}
				}
			}
		}
	}
}
