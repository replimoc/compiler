package compiler.semantic;

import java.io.File;
import java.io.FileWriter;
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
public class AutomatedSemanticCheckTest implements TestFileVisitor.FileTester {

	private static final String SEMANTIC_CHECK_EXTENSION = ".sc";

	@Test
	public void testCheckFiles() throws Exception {
		Path testDir = Paths.get("testdata");
		// TestFileVisitor lexTester = new TestFileVisitor(SEMANTIC_CHECK_EXTENSION, this, "multiarrays");
		TestFileVisitor lexTester = new TestFileVisitor(SEMANTIC_CHECK_EXTENSION, this);
		Files.walkFileTree(testDir, lexTester);
		lexTester.checkForFailedTests();
	}

	@Override
	public void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception {

		System.out.println("Testing file = " + sourceFilePath + "----------------------------------------------->");

		// read expected results file
		List<String> lines = Files.readAllLines(expectedResultFilePath, StandardCharsets.US_ASCII);
		boolean isErrorExpected = !"correct".equals(lines.get(0));
		int err_num = lines.size() > 1 ? Integer.parseInt(lines.get(1)) : -1;

		// start lexer
		Lexer lexer = new Lexer(Files.newBufferedReader(sourceFilePath, StandardCharsets.US_ASCII), new StringTable());
		Parser parser = new Parser(lexer);

		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(parser.parse());
		if (isErrorExpected) {
			if (!semanticResult.hasErrors()) {
				Assert.fail("semantic analysis succeeded on incorrect program: " + sourceFilePath);

			} else if (err_num == semanticResult.getNumberOfExceptions()) {
				// Incorrect program produces the right errors, write them to a log file
				File file = new File(expectedResultFilePath.toFile().getPath() + ".errors");
				FileWriter output = new FileWriter(file, false);
				for (SemanticAnalysisException error : semanticResult.getExceptions()) {
					output.append(error.toString() + "\n");
				}
				output.close();
				output = null;
				file = null;

			} else {
				for (SemanticAnalysisException error : semanticResult.getExceptions()) {
					System.out.println("error.toString() = " + error.toString());
				}
			}
			if (err_num > 0)
			{
				Assert.assertEquals("wrong number of errors", err_num, semanticResult.getNumberOfExceptions());
			}
		} else {
			if (semanticResult.hasErrors()) {
				System.out.println("");
				System.out.println("----------------------------------------------------------------------------");
				System.err.println("Test for file = " + sourceFilePath + " failed");
				for (SemanticAnalysisException error : semanticResult.getExceptions()) {
					error.printStackTrace();
				}
				System.out.println("----------------------------------------------------------------------------");
				System.out.println("");
				Assert.fail("semantic analysis failed on correct program: " + sourceFilePath);
			}
		}

		System.err.println("<-----------------------------------------------file = " + sourceFilePath + " has passed the test");

	}
}
