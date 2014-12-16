package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.AssemblerOperation;
import firm.Graph;
import firm.Program;

public final class AssemblerGenerator {
	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile) throws IOException {
		HashMap<Graph, List<AssemblerOperation>> assembler = new HashMap<>();

		for (Graph graph : Program.getGraphs()) {
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor();
			graph.walkTopological(visitor);
			assembler.put(graph, visitor.getAssembler());
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void generateAssemblerFile(Path outputFile, HashMap<Graph, List<AssemblerOperation>> assembler) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);

		for (Entry<Graph, List<AssemblerOperation>> graphAssembler : assembler.entrySet()) {
			for (AssemblerOperation operation : graphAssembler.getValue()) {
				writer.write(operation.toString());
			}
		}
	}
}
