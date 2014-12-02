package compiler.firm;

import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compiler.Utils;
import compiler.ast.AstNode;
import compiler.parser.Parser;
import compiler.semantic.SemanticCheckResults;
import compiler.semantic.SemanticChecker;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestUtils;

/**
 * Test case for correct output of compiler with only parse phase
 * <p/>
 * this test should be started from Compiler directory
 */
public class AutomatedOutputComparisonTest implements TestFileVisitor.FileTester {

	private static final String OUTPUT_FILE_EXTENSION = ".result";

	@Before
	public void initFirm() {
		FirmUtils.initFirm();
	}

	@Test
	public void testCompareOutputWithJava() throws Exception {
		Path testDir = Paths.get("testdata");
		TestFileVisitor parserTester = new TestFileVisitor(OUTPUT_FILE_EXTENSION, this);
		Files.walkFileTree(testDir, parserTester);
		parserTester.checkForFailedTests();
	}

	@After
	public void finishFirm() {
		FirmUtils.finishFirm();
	}

	@Override
	public void testSourceFile(Path sourceFile, Path expectedFile) throws Exception {
		System.out.println("Testing execution result of " + sourceFile);

		AstNode ast;
		SemanticCheckResults semanticResults;
		Parser parser = TestUtils.initParser(sourceFile);

		ast = parser.parse();
		semanticResults = SemanticChecker.checkSemantic(ast);
		assertFalse(semanticResults.hasErrors());

		FirmGraphGenerator.transformToFirm(ast, semanticResults.getClassScopes());
		String generatedExecutable = FirmUtils.createBinary("tmp/a");

		List<String> actualOutput = Utils.systemExec(generatedExecutable);
		List<String> expectedOutput = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
		TestUtils.assertLinesEqual(sourceFile, expectedOutput, actualOutput.iterator());
	}
}
