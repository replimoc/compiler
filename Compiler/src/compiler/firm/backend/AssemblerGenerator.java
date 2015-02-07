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

import compiler.firm.backend.FirmGraphTraverser.BlockInfo;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.FunctionSpecificationOperation;
import compiler.firm.backend.operations.P2AlignOperation;
import compiler.firm.backend.operations.TextOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.registerallocation.ssa.AssemblerOperationsBlock;
import compiler.firm.backend.registerallocation.ssa.AssemblerProgram;
import compiler.firm.backend.registerallocation.ssa.MustSpillException;
import compiler.firm.backend.registerallocation.ssa.SimpleSsaSpiller;
import compiler.firm.backend.registerallocation.ssa.SsaRegisterAllocator;

import firm.BackEdges;
import firm.BlockWalker;
import firm.Graph;
import firm.Program;
import firm.bindings.binding_irdom;
import firm.nodes.Block;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, final CallingConvention callingConvention, boolean doPeephole, boolean noRegisters,
			boolean debugRegisterAllocation) throws IOException {
		SsaRegisterAllocator.setDebuggingMode(debugRegisterAllocation);

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
			graph.check();
			binding_irdom.compute_doms(graph.ptr);
			binding_irdom.compute_postdoms(graph.ptr);

			if (debugRegisterAllocation)
				System.out.println(graph.getEntity().getLdName());

			BlockNodesCollectingVisitor collectorVisitor = new BlockNodesCollectingVisitor();
			graph.walkTopological(collectorVisitor);

			HashMap<Block, BlockNodes> nodesPerBlockMap = collectorVisitor.getNodesPerBlockMap();
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);

			BackEdges.enable(graph);
			HashMap<Block, BlockInfo> blockInfos = FirmGraphTraverser.calculateBlockInfos(graph);
			FirmGraphTraverser.walkBlocksAllocationFriendly(graph, blockInfos, new BlockNodesWalker(visitor, nodesPerBlockMap));
			BackEdges.disable(graph);

			AssemblerProgram assemblerProgram = visitor.getAssemblerProgram(graph);

			if (debugRegisterAllocation)
				generatePlainAssemblerFile(Paths.get(graph.getEntity().getLdName() + ".plain"), assemblerProgram, blockInfos);

			allocateRegistersSsa(graph, assemblerProgram, noRegisters);

			ArrayList<AssemblerOperation> operationsList = generateOperationsList(assemblerProgram, blockInfos);
			if (doPeephole) {
				PeepholeOptimizer peepholeOptimizer = new PeepholeOptimizer(operationsList, assembler);
				peepholeOptimizer.optimize();
			} else {
				assembler.addAll(operationsList);
			}
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void allocateRegistersSsa(Graph graph, AssemblerProgram program, boolean noRegisters) {
		// SplittingSsaSpiller splittingSsaSpiller = new SplittingSsaSpiller(program);
		// splittingSsaSpiller.reduceRegisterPressure(2, true);

		SimpleSsaSpiller ssaSpiller = new SimpleSsaSpiller(program);
		RegisterAllocationPolicy policy;
		if (noRegisters) {
			policy = RegisterAllocationPolicy.NO_REGISTERS;
			ssaSpiller.reduceRegisterPressure(policy.getNumberOfRegisters(Bit.BIT64), true);
		} else {
			try {
				policy = RegisterAllocationPolicy.BP_B_12_13_14_15_A__DI_SI_D_C_8_9_10_11;
				ssaSpiller.reduceRegisterPressure(policy.getNumberOfRegisters(Bit.BIT64), false);
			} catch (MustSpillException e) {
				SsaRegisterAllocator.debugln("Cannot use large policy, using small policy with spilling");
				policy = RegisterAllocationPolicy.BP_B_12_13_14_15_A__DI_SI_D_C_8;
				ssaSpiller.reduceRegisterPressure(policy.getNumberOfRegisters(Bit.BIT64), true);
			}
		}

		SsaRegisterAllocator ssaAllocator = new SsaRegisterAllocator(program);
		ssaAllocator.colorGraph(policy);
		program.setDummyOperationsInformation(ssaSpiller.getCurrentStackOffset());
	}

	private static ArrayList<AssemblerOperation> generateOperationsList(final AssemblerProgram program, HashMap<Block, BlockInfo> blockInfos) {
		final ArrayList<AssemblerOperation> operationsList = new ArrayList<>();

		FirmGraphTraverser.walkLoopOptimizedPostorder(program.getGraph(), blockInfos, new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				operationsList.addAll(program.getOperationsBlock(block).getOperations());
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

	private static void generatePlainAssemblerFile(Path outputFile, final AssemblerProgram assemblerProgram, HashMap<Block, BlockInfo> blockInfos) {
		try {
			final BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);

			FirmGraphTraverser.walkBlocksAllocationFriendly(assemblerProgram.getGraph(), blockInfos, new BlockWalker() {
				@Override
				public void visitBlock(Block block) {
					try {
						AssemblerOperationsBlock operationsBlock = assemblerProgram.getOperationsBlock(block);
						if (operationsBlock == null)
							return;

						for (AssemblerOperation operation : operationsBlock.getOperations()) {

							writer.write(operation.toString() + " # r:" + operation.getReadRegisters() + "; w:" + operation.getWriteRegisters());
							writer.newLine();

						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});

			writer.close();

		} catch (IOException | RuntimeException e) {
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
