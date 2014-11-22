package compiler.semantic;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.utils.TestFileVisitor;

/**
 * Test correctness of semantic analysis che
 */
public class CheckOutputTest implements TestFileVisitor.FileTester {

	private static final String SEMANTIC_CHECK_EXTENSION = ".sc";

	@Test
	public void testCheckFiles() throws Exception {
		Path testDir = Paths.get("testdata");
		TestFileVisitor lexTester = new TestFileVisitor(SEMANTIC_CHECK_EXTENSION, this);
		Files.walkFileTree(testDir, lexTester);
		lexTester.checkForFailedTests();
	}

	@Override
	public void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception {

		System.err.println("Testing file = " + sourceFilePath + "----------------------------------------------->");

		// read expected results file
		List<String> lines = Files.readAllLines(expectedResultFilePath, StandardCharsets.US_ASCII);
		boolean isErrorExpected = !"correct".equals(lines.get(0));
		int err_num = lines.size() > 1 ? Integer.parseInt(lines.get(1)) : -1;

		// start lexer
		Lexer lexer = new Lexer(Files.newBufferedReader(sourceFilePath, StandardCharsets.US_ASCII), new StringTable());
		Parser parser = new Parser(lexer);

		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(parser.parse());
		if (isErrorExpected) {
			if (errors.size() == 0)
				Assert.fail("semantic analysis succeeded on incorrect program: " + sourceFilePath);

			for (SemanticAnalysisException error : errors) {
				System.err.println("error.toString() = " + error.toString());
			}
			if (err_num > 0)
			{
				Assert.assertEquals("wrong number of errors", err_num, errors.size());
			}
		} else {
			if (!errors.isEmpty()) {
				System.err.println("");
				System.err.println("----------------------------------------------------------------------------");
				System.err.println("Test for file = " + sourceFilePath + " failed");
				for (SemanticAnalysisException error : errors) {
					error.printStackTrace();
				}
				System.err.println("----------------------------------------------------------------------------");
				System.err.println("");
				Assert.fail("semantic analysis failed on correct program: " + sourceFilePath);
			}
		}

		System.err.println("<-----------------------------------------------file = " + sourceFilePath + " has passed the test");

	}
}
