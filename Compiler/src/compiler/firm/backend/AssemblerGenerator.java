package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.ast.declaration.MainMethodDeclaration;
import compiler.firm.backend.FirmGraphTraverser.BlockInfo;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.FunctionSpecificationOperation;
import compiler.firm.backend.operations.P2AlignOperation;
import compiler.firm.backend.operations.TextOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.LinearScanRegisterAllocation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import firm.BackEdges;
import firm.BlockWalker;
import firm.Graph;
import firm.Program;
import firm.nodes.Block;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, final CallingConvention callingConvention, boolean doPeephole, boolean noRegisters)
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

			// final NodeNumberPrintingVisitor printer = new NodeNumberPrintingVisitor();

			HashMap<Block, BlockNodes> nodesPerBlockMap = collectorVisitor.getNodesPerBlockMap();
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);
			BackEdges.enable(graph);
			HashMap<Block, BlockInfo> blockInfos = FirmGraphTraverser.walkBlocksPostOrder(graph, new BlockNodesWalker(visitor, nodesPerBlockMap));
			BackEdges.disable(graph);

			visitor.finishOperationsList();
			ArrayList<AssemblerOperation> operationsBlocksPostOrder = visitor.getAllOperations();
			final HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks = visitor.getOperationsOfBlocks();

			// TODO remove next line when it's not needed any more
			// generatePlainAssemblerFile(Paths.get(graph.getEntity().getLdName() + ".plain"), operationsBlocksPostOrder);

			allocateRegisters(graph, operationsBlocksPostOrder, noRegisters);

			operationsBlocksPostOrder.clear(); // free some memory

			ArrayList<AssemblerOperation> operationsList = generateOperationsList(graph, blockInfos, operationsOfBlocks);

			if (doPeephole) {
				PeepholeOptimizer peepholeOptimizer = new PeepholeOptimizer(operationsList, assembler);
				peepholeOptimizer.optimize();
			} else {
				assembler.addAll(operationsList);
			}
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void allocateRegisters(Graph graph, ArrayList<AssemblerOperation> operationsBlocksPostOrder, boolean noRegisters) {
		boolean isMain = MainMethodDeclaration.MAIN_METHOD_NAME.equals(graph.getEntity().getLdName());
		RegisterAllocationPolicy regsiterPolicy = noRegisters ? RegisterAllocationPolicy.NO_REGISTERS
				: RegisterAllocationPolicy.ALL_A_B_C_D_8_9_10_11_12_DI_SI;
		new LinearScanRegisterAllocation(regsiterPolicy, isMain, operationsBlocksPostOrder).allocateRegisters();
	}

	private static ArrayList<AssemblerOperation> generateOperationsList(Graph graph, HashMap<Block, BlockInfo> blockInfos,
			final HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		final ArrayList<AssemblerOperation> operationsList = new ArrayList<>();

		FirmGraphTraverser.walkLoopOptimizedPostorder(graph, blockInfos, new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				operationsList.addAll(operationsOfBlocks.get(block));
			}
		});

		return operationsList;
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

	private static class BlockNodesWalker implements BlockWalker {
		private final BulkPhiNodeVisitor visitor;
		private final HashMap<Block, BlockNodes> nodesPerBlockMap;

		public BlockNodesWalker(BulkPhiNodeVisitor visitor, HashMap<Block, BlockNodes> nodesPerBlockMap) {
			this.visitor = visitor;
			this.nodesPerBlockMap = nodesPerBlockMap;
		}

		@Override
		public void visitBlock(Block block) {
			nodesPerBlockMap.get(block).visitNodes(visitor, nodesPerBlockMap);
		}
	}
}
