package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import compiler.firm.backend.operations.templates.AssemblerOperation;

import firm.BlockWalker;
import firm.Graph;
import firm.nodes.Block;

public class SsaRegisterAllocator {
	private static boolean DEBUG = true;

	private final HashMap<Block, AssemblerOperationsBlock> operationsBlocks;

	public SsaRegisterAllocator(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
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

		if (DEBUG) {
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

}
