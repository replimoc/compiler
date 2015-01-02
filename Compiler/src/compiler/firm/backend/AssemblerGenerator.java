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
import firm.Graph;
import firm.Program;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, HashMap<String, CallingConvention> callingConvention) throws IOException {
		final List<AssemblerOperation> assembler = new ArrayList<>();

		assembler.add(new TextOperation());
		assembler.add(new P2AlignOperation());

		for (Graph graph : Program.getGraphs()) {
			assembler.add(new FunctionSpecificationOperation(graph.getEntity().getLdName()));
		}

		for (Graph graph : Program.getGraphs()) {
			BlockNodesCollectingVisitor collectorVisitor = new BlockNodesCollectingVisitor();
			graph.walkTopological(collectorVisitor);

			final List<BlockNodes> nodesPerBlock = collectorVisitor.getNodesPerBlock();
			final X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);
			final NodeNumberPrintingVisitor printer = new NodeNumberPrintingVisitor();

			visitor.addListOfAllPhis(collectorVisitor.getAllPhis());

			BackEdges.enable(graph);
			for (BlockNodes blockNodes : nodesPerBlock) {
				blockNodes.visitNodes(printer, collectorVisitor.getNodesPerBlockMap());
				blockNodes.visitNodes(visitor, collectorVisitor.getNodesPerBlockMap());
			}
			BackEdges.disable(graph);

			List<AssemblerOperation> operations = visitor.getOperations();
			allocateRegisters(operations);
			assembler.addAll(operations);
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
}
