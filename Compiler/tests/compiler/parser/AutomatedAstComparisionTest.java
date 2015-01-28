package compiler.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	public static final String PRETTY_AST_EXTENSION = ".pretty";
	private static final Object DISABLE_NESTING_IS_FUN = "DISABLE_NESTING_IS_FUN";

	@Test
	public void testParserFiles() throws Exception {
		TestFileVisitor.runTests(this, "testdata", TestFileVisitor.JAVA_EXTENSION, PRETTY_AST_EXTENSION);
	}

	@Test
	public void testFixpointProperty() throws Exception {
		TestFileVisitor.runTests(this, "testdata", PRETTY_AST_EXTENSION, PRETTY_AST_EXTENSION);
	}

	@Test
	public void testMjTestPositive() throws Exception {
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/pos-parser");
	}

	@Test
	public void testMjTestPositiveCompile() throws Exception {
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/pos");
	}

	@Test
	public void testMjTestPositiveCompileRun() throws Exception {
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/run");
	}

	@Override
	public void testSourceFile(TestFileVisitor visitor, Path sourceFile, Path expectedFile) throws Exception {
		if (sourceFile.endsWith("nesting-is-fun.mj")) {
			Map<String, String> env = System.getenv();
			if (env.containsKey(DISABLE_NESTING_IS_FUN)) {
				System.out.println("Nesting is fun is disabled, skipping!");
				return;
			}
		}

		// read expected output
		List<String> expectedOutput = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
		boolean failingExpected = isParserFailExpected(expectedOutput);
		boolean isFixpoint = sourceFile.equals(expectedFile);
		if (isFixpoint && failingExpected) { // this is the case if we do the fixpoint test
			return; // do not test error files
		}

		System.out.println("Testing ast generation of " + sourceFile);

		try {
			Iterator<String> expectedOutputIterator = expectedOutput.iterator();
			StringBuilder printedAst = runParser(sourceFile);
			if (isFixpoint) { // Run parser again
				sourceFile = TestUtils.writeToTemporaryFile(printedAst).toPath();
				expectedOutputIterator = new Scanner(new StringBuilderReader(printedAst));
				((Scanner) expectedOutputIterator).useDelimiter("\n");
				printedAst = runParser(sourceFile);
			}

			Scanner s = new Scanner(new StringBuilderReader(printedAst));
			s.useDelimiter("\n"); // separate at new lines

			TestUtils.assertLinesEqual(sourceFile, expectedOutputIterator, s);
			s.close();

			assertFalse(failingExpected);
		} catch (ParsingFailedException e) {
			List<ParserException> errors = e.getDetectedErrors();

			if (failingExpected) {
				if (expectedOutput.size() > 1) {
					if (Integer.parseInt(expectedOutput.get(1)) == errors.size()) {
						return;
					}
				} else {
					return; // ok;
				}
			}

			failParsingException(e);
		}
	}

	public static boolean isParserFailExpected(List<String> expectedOutput) {
		return !expectedOutput.isEmpty() && "error".equals(expectedOutput.get(0));
	}

	private static void failParsingException(ParsingFailedException e) {
		e.printParserExceptions();
		fail();
	}

	private StringBuilder runParser(Path sourceFile) throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(sourceFile);
		AstNode ast = parser.parse();
		return PrettyPrinter.prettyPrint(ast);
	}
}
