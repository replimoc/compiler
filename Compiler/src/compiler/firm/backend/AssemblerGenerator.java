package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.FunctionSpecificationOperation;
import compiler.firm.backend.operations.P2AlignOperation;
import compiler.firm.backend.operations.TextOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;

import firm.BackEdges;
import firm.BlockWalker;
import firm.Graph;
import firm.Program;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Node;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, HashMap<String, CallingConvention> callingConvention, boolean doPeephole)
			throws IOException {
		final ArrayList<AssemblerOperation> assembler = new ArrayList<>();

		assembler.add(new TextOperation());
		assembler.add(new P2AlignOperation());

		for (Graph graph : Program.getGraphs()) {
			assembler.add(new FunctionSpecificationOperation(graph.getEntity().getLdName()));
		}

		for (Graph graph : Program.getGraphs()) {
			// System.out.println(graph.getEntity().getLdName());

			BlockNodesCollectingVisitor collectorVisitor = new BlockNodesCollectingVisitor();
			graph.walkTopological(collectorVisitor);

			final X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);
			// final NodeNumberPrintingVisitor printer = new NodeNumberPrintingVisitor();

			visitor.addListOfAllPhis(collectorVisitor.getAllPhis());

			BackEdges.enable(graph);

			final HashMap<Block, BlockNodes> nodesPerBlockMap = collectorVisitor.getNodesPerBlockMap();
			walkBlocksPostOrder(graph, new BlockWalker() {
				@Override
				public void visitBlock(Block block) {
					nodesPerBlockMap.get(block).visitNodes(visitor, nodesPerBlockMap);
				}
			});
			BackEdges.disable(graph);

			ArrayList<AssemblerOperation> operations = visitor.getOperations();

			// TODO remove next line when it's not needed any more
			// generatePlainAssemblerFile(Paths.get(graph.getEntity().getLdName() + ".plain"), operations);

			allocateRegisters(operations);
			if (doPeephole) {
				PeepholeOptimizer peepholeOptimizer = new PeepholeOptimizer(operations, assembler);
				peepholeOptimizer.optimize();
			} else {
				assembler.addAll(operations);
			}
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void walkBlocksPostOrder(Graph graph, BlockWalker walker) {
		HashMap<Block, ? extends List<Block>> blockFollowers = calculateBlockFollowers(graph);
		incrementBlockVisited(graph);
		traverseBlocksDepthFirst(graph.getStartBlock(), blockFollowers, walker);
	}

	private static HashMap<Block, ? extends List<Block>> calculateBlockFollowers(Graph graph) {
		final HashMap<Block, LinkedList<Block>> blockFollowers = new HashMap<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				for (Node pred : block.getPreds()) {
					Block predBlock = (Block) pred.getBlock();

					LinkedList<Block> predsNextBlocks = blockFollowers.get(predBlock);
					if (predsNextBlocks == null) {
						predsNextBlocks = new LinkedList<Block>();
						blockFollowers.put(predBlock, predsNextBlocks);
					}

					// adding the block at the beginning ensures that the loop body comes after the head
					// adding the block to the end of the list makes depth first to the end block and then the loop body
					predsNextBlocks.addFirst(block);
				}
			}
		});
		return blockFollowers;
	}

	private static void traverseBlocksDepthFirst(Block block, HashMap<Block, ? extends List<Block>> nextBlocks, BlockWalker walker) {
		block.markBlockVisited();
		walker.visitBlock(block);

		List<Block> followers = nextBlocks.get(block);

		if (followers == null)
			return;

		for (Block followerBlock : followers) {
			if (!followerBlock.blockVisited())
				traverseBlocksDepthFirst(followerBlock, nextBlocks, walker);
		}
	}

	private static void incrementBlockVisited(Graph graph) {
		binding_irgraph.inc_irg_block_visited(graph.ptr);
	}

	private static void allocateRegisters(List<AssemblerOperation> assembler) {
		LinearScanRegisterAllocation registerAllocation = new LinearScanRegisterAllocation(assembler);
		registerAllocation.allocateRegisters();
	}

	private static void generateAssemblerFile(Path outputFile, List<AssemblerOperation> assembler) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);

		for (AssemblerOperation operation : assembler) {
			for (String operationString : operation.toStringWithSpillcode()) {
				writer.write(operationString);
				writer.newLine();
			}
		}
		writer.close();
	}

	private static void generatePlainAssemblerFile(Path outputFile, List<AssemblerOperation> operations) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);
			for (AssemblerOperation operation : operations) {
				writer.write(operation.toString());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
