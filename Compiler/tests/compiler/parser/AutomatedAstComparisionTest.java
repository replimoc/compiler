package compiler.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

	@Override
	public void testSourceFile(Path sourceFile, Path parserFile) throws Exception {
		System.out.println("Testing ast generation of " + sourceFile);
		// read expected output
		List<String> expectedOutput = Files.readAllLines(parserFile, StandardCharsets.US_ASCII);
		Parser parser = TestUtils.initParser(sourceFile);

		try {
			AstNode ast = parser.parse();

			String printedAst = PrettyPrinter.prettyPrint(ast);
			Scanner s = new Scanner(printedAst);

			int line = 1;
			for (String expectedLine : expectedOutput) {
				assertEquals("Error in file: " + sourceFile + " in line: " + line, expectedLine, s.nextLine());
				line++;
			}
			assertFalse(s.hasNext());
			s.close();

		} catch (ParsingFailedException e) {
			int errors = e.getDetectedErrors();
			assertEquals(expectedOutput.get(0), "error");
			if (expectedOutput.size() > 1) {
				assertEquals(Integer.parseInt(expectedOutput.get(1)), errors);
			}
		}
	}
}
