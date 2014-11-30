package compiler.firm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Ignore;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.semantic.SemanticCheckResults;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;

import firm.Dump;
import firm.Graph;
import firm.Program;

/**
 * TODO to be firm test
 */
@Ignore
public class FirmTester {

	public static void main(String[] args) throws Exception {
		FirmUtils.initFirm();

		String filename = "firmdata/classes.java";

		Lexer lexer = new Lexer(Files.newBufferedReader(Paths.get(filename), StandardCharsets.US_ASCII), new StringTable());
		Parser parser = new Parser(lexer);
		compiler.ast.Program program = parser.parse();
		SemanticCheckResults semanticResults = SemanticChecker.checkSemantic(program);
		if (semanticResults.hasErrors()) {
			for (SemanticAnalysisException error : semanticResults.getExceptions()) {
				error.printStackTrace();
			}
			throw new Exception("program is not semantically correct");
		}

		Transformation.transformToFirm(program, semanticResults.getClassScopes());

		// FirmGenerationVisitor firmGen = new FirmGenerationVisitor();
		// program.accept(firmGen);

		for (Graph g : Program.getGraphs()) {
			g.check();
			Dump.dumpGraph(g, "--finished");
		}

		FirmUtils.finishFirm();
	}

}
