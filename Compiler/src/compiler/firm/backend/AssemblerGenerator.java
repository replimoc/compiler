package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.registerallocation.linear.InterferenceGraph;
import compiler.firm.backend.registerallocation.linear.LinearScanRegisterAllocation;
import compiler.firm.backend.registerallocation.ssa.AssemblerProgram;
import compiler.firm.backend.registerallocation.ssa.SsaRegisterAllocator;

import firm.BackEdges;
import firm.BlockWalker;
import firm.Graph;
import firm.Program;
import firm.nodes.Block;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, final CallingConvention callingConvention, boolean doPeephole, boolean noRegisters,
			boolean debugRegisterAllocation) throws IOException {
		InterferenceGraph.setDebuggingMode(debugRegisterAllocation);

		final ArrayList<AssemblerOperation> assembler = new ArrayList<>();

		assembler.add(new TextOperation());
		assembler.add(new P2AlignOperation());

		for (Graph graph : Program.getGraphs()) {
			BackEdges.enable(graph);
			graph.walk(new InsertBlockAfterConditionVisitor());
			BackEdges.disable(graph);
			assembler.add(new FunctionSpecificationOperation(graph.getEntity().getLdName()));
		}

		for (Graph graph : Program.getGraphs()) {
			if (debugRegisterAllocation)
				System.out.println(graph.getEntity().getLdName());

			BlockNodesCollectingVisitor collectorVisitor = new BlockNodesCollectingVisitor();
			graph.walkTopological(collectorVisitor);

			// final NodeNumberPrintingVisitor printer = new NodeNumberPrintingVisitor();

			HashMap<Block, BlockNodes> nodesPerBlockMap = collectorVisitor.getNodesPerBlockMap();
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);

			BackEdges.enable(graph);
			HashMap<Block, BlockInfo> blockInfos = FirmGraphTraverser.calculateBlockInfos(graph);
			FirmGraphTraverser.walkBlocksAllocationFriendly(graph, blockInfos, new BlockNodesWalker(visitor, nodesPerBlockMap));
			BackEdges.disable(graph);

			visitor.finishOperationsList();
			ArrayList<AssemblerOperation> operationsBlocksPostOrder = visitor.getAllOperations();
			final HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks = visitor.getOperationsOfBlocks();

			if (debugRegisterAllocation)
				generatePlainAssemblerFile(Paths.get(graph.getEntity().getLdName() + ".plain"), operationsBlocksPostOrder);

			// allocateRegistersLinear(graph, operationsBlocksPostOrder, noRegisters, debugRegisterAllocation);
			allocateRegistersSsa(graph, operationsOfBlocks);

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

	private static void allocateRegistersLinear(Graph graph, ArrayList<AssemblerOperation> operationsBlocksPostOrder, boolean noRegisters,
			boolean debugRegisterAllocation) {
		boolean isMain = MainMethodDeclaration.MAIN_METHOD_NAME.equals(graph.getEntity().getLdName());

		new LinearScanRegisterAllocation(isMain, operationsBlocksPostOrder).allocateRegisters(debugRegisterAllocation, noRegisters);
	}

	private static void allocateRegistersSsa(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		boolean isMain = MainMethodDeclaration.MAIN_METHOD_NAME.equals(graph.getEntity().getLdName());

		AssemblerProgram program = new AssemblerProgram(graph, operationsOfBlocks);
		SsaRegisterAllocator ssaAllocator = new SsaRegisterAllocator(program);
		RegisterAllocationPolicy policy = RegisterAllocationPolicy.A_BP_B_12_13_14_15__DI_SI_D_C_8_9_10_11;
		ssaAllocator.colorGraph(graph, policy);
		program.setDummyOperationsInformation(policy.getAllowedBundles(Bit.BIT64), 0, isMain, policy);
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
				writer.write(operation.toString() + " # r:" + operation.getReadRegisters() + "; w:" + operation.getWriteRegisters());
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
