package compiler.firm.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.ast.CallingConvention;
import compiler.firm.backend.operations.AssemblerOperation;
import compiler.firm.backend.operations.FunctionSpecificationOperation;
import compiler.firm.backend.operations.P2AlignOperation;
import compiler.firm.backend.operations.TextOperation;

import firm.Graph;
import firm.Program;

public final class AssemblerGenerator {

	private AssemblerGenerator() {
	}

	public static void createAssemblerX8664(Path outputFile, HashMap<String, CallingConvention> callingConvention) throws IOException {
		List<AssemblerOperation> assembler = new ArrayList<>();

		assembler.add(new TextOperation());
		assembler.add(new P2AlignOperation());

		for (Graph graph : Program.getGraphs()) {
			assembler.add(new FunctionSpecificationOperation(graph.getEntity().getLdName()));
		}

		for (Graph graph : Program.getGraphs()) {
			X8664AssemblerGenerationVisitor visitor = new X8664AssemblerGenerationVisitor(callingConvention);
			graph.walkTopological(visitor);
			assembler.addAll(visitor.getAssembler());
		}

		generateAssemblerFile(outputFile, assembler);
	}

	private static void generateAssemblerFile(Path outputFile, List<AssemblerOperation> assembler) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.US_ASCII);

		for (AssemblerOperation operation : assembler) {
			writer.write(operation.toString());
		}
		writer.close();
	}
}
