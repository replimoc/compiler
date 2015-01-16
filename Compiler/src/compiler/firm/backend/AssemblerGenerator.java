package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
import firm.nodes.Block;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, CallingConvention callingConvention, boolean doPeephole)
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
			FirmGraphTraverser.walkBlocksPostOrder(graph, new BlockWalker() {
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
