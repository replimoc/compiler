package compiler.firm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import compiler.StringTable;
import compiler.ast.ClassMember;
import compiler.ast.MethodDeclaration;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;
import firm.ClassType;
import firm.Construction;
import firm.Dump;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.PrimitiveType;
import firm.Program;
import firm.Type;
import firm.nodes.Node;

public class FirmMethodsTest {

	@Test
	public void testjFirmInit() throws Exception {
		FirmUtils.initFirm();

		compiler.ast.Program ast = getAstForFile("firmdata/methodstest.java");
		assertEquals(1, ast.getClasses().size());
		
		FirmGenerationVisitor firmGen = new FirmGenerationVisitor();
		for (ClassMember classMember : ast.getClasses().get(0).getMembers()) {
			classMember.accept(firmGen);
		}

		for (Graph g : Program.getGraphs()) {
			g.check();
			Dump.dumpGraph(g, "--finished");
		}

		FirmUtils.finishFirm();
	}
	
	private compiler.ast.Program getAstForFile(String fileName) throws Exception {
		Lexer lexer = new Lexer(Files.newBufferedReader(Paths.get(fileName), StandardCharsets.US_ASCII), new StringTable());
		Parser parser = new Parser(lexer);
		compiler.ast.Program program = parser.parse();
		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(program);
		if (errors.size() != 0) {
			for (SemanticAnalysisException error : errors) {
				error.printStackTrace();
			}
			throw new Exception("program is not semantically correct");
		}
		
		return program;
	}
}
