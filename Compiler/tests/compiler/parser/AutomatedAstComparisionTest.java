package compiler.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import compiler.ast.AstNode;
import compiler.parser.printer.PrettyPrinter;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestUtils;

/**
 * Test case for correct output of compiler with only parse phase
 * <p/>
 * this test should be started from Compiler directory
 */
public class AutomatedAstComparisionTest implements TestFileVisitor.FileTester {

	private static final String PRETTY_AST_EXTENSION = ".pretty";

	@Test
	public void testParserFiles() throws Exception {
		Path testDir = Paths.get("testdata");
		TestFileVisitor parserTester = new TestFileVisitor(PRETTY_AST_EXTENSION, this);
		Files.walkFileTree(testDir, parserTester);
		parserTester.checkForFailedTests();
	}

	@Test
	public void testFixpointProperty() throws Exception {
		Path testDir = Paths.get("testdata");
		TestFileVisitor parserTester = new TestFileVisitor(PRETTY_AST_EXTENSION, PRETTY_AST_EXTENSION, this);
		Files.walkFileTree(testDir, parserTester);
		parserTester.checkForFailedTests();
	}

	@Override
	public void testSourceFile(Path sourceFile, Path expectedFile) throws Exception {
		// read expected output
		List<String> expectedOutput = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
		boolean failingExpected = !expectedOutput.isEmpty() && "error".equals(expectedOutput.get(0));

		if (sourceFile.equals(expectedFile) && failingExpected) { // this is the case if we do the fixpoint test
			return; // do not test error files
		}

		System.out.println("Testing ast generation of " + sourceFile);
		Parser parser = TestUtils.initParser(sourceFile);

		try {
			AstNode ast = parser.parse();
			String printedAst = PrettyPrinter.prettyPrint(ast);

			Scanner s = new Scanner(printedAst);
			TestUtils.assertLinesEqual(sourceFile, expectedOutput, s);
			s.close();

			assertFalse(failingExpected);
		} catch (ParsingFailedException e) {
			int errors = e.getDetectedErrors();
			assertTrue(failingExpected);
			if (expectedOutput.size() > 1) {
				assertEquals(Integer.parseInt(expectedOutput.get(1)), errors);
			}
		}
	}
}
