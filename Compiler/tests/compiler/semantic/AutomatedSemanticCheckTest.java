package compiler.semantic;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compiler.StringTable;
import compiler.ast.Program;
import compiler.lexer.Lexer;
import compiler.parser.AutomatedAstComparisionTest;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestUtils;

/**
 * Test correctness of semantic analysis
 */
public class AutomatedSemanticCheckTest implements TestFileVisitor.FileTester {

	private static final String SEMANTIC_CHECK_EXTENSION = ".sc";
	private boolean allCorrect;

	@Test
	public void testCheckFiles() throws Exception {
		allCorrect = true;
		TestFileVisitor.runTests(this, "testdata", TestFileVisitor.JAVA_EXTENSION, TestFileVisitor.JAVA_EXTENSION);
	}

	@Test
	public void testCheckMjTestFiles() throws Exception {
		allCorrect = true;
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/pos");
	}

	@Test
	public void testCheckMjTestFilesRunnable() throws Exception {
		allCorrect = true;
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/run");
	}

	@Test
	public void testCheckMjTestFilesNegativeParserAndSemantic() throws Exception {
		allCorrect = false;
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/neg");
	}

	@Test
	public void testCheckMjTestFilesPositiveParserAndSemantic() throws Exception {
		allCorrect = false; // Positive files should be moved to pos
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/pos-parser");
	}

	@Override
	public void testSourceFile(TestFileVisitor visitor, Path sourceFilePath, Path expectedResultFilePath) throws Exception {
		// check preconditions
		Path prettyFile = visitor.getFileWithEnding(sourceFilePath, AutomatedAstComparisionTest.PRETTY_AST_EXTENSION);
		if (Files.exists(prettyFile) && AutomatedAstComparisionTest.isParserFailExpected(Files.readAllLines(prettyFile, StandardCharsets.US_ASCII))) {
			return;
		}
		if (TestFileVisitor.JAVA_EXTENSION.equals(visitor.getSourceFileExtension()) && sourceFilePath.toString().contains("mj-test")) {
			return;
		}

		System.out.println("Testing file = " + sourceFilePath + "----------------------------------------------->");

		// read expected results file
		Path checkFile = visitor.getFileWithEnding(sourceFilePath, SEMANTIC_CHECK_EXTENSION);
		List<String> lines = Arrays.asList(allCorrect ? "correct" : "error");
		if (Files.exists(checkFile)) {
			lines = Files.readAllLines(checkFile, StandardCharsets.US_ASCII);
		}
		boolean isErrorExpected = !"correct".equals(lines.get(0));
		int expectedNumberOfErrors = lines.size() > 1 ? Integer.parseInt(lines.get(1)) : -1;

		// start lexer
		StringTable stringTable = new StringTable();
		Lexer lexer = new Lexer(Files.newBufferedReader(sourceFilePath, StandardCharsets.US_ASCII), stringTable);
		Parser parser = new Parser(lexer);
		ParsingFailedException parsingError = null;
		Program parserResult = null;
		try {
			parserResult = parser.parse();
		} catch (ParsingFailedException e) {
			parsingError = e;
		}

		SemanticCheckResults semanticResult = null;
		if (parsingError == null) {
			semanticResult = SemanticChecker.checkSemantic(parserResult, stringTable);
		}
		if (isErrorExpected) {
			if (parsingError == null && !semanticResult.hasErrors()) {
				Assert.fail("semantic analysis succeeded on incorrect program: " + sourceFilePath);
			} else if (parsingError == null && expectedNumberOfErrors == semanticResult.getNumberOfExceptions()) {
				// Incorrect program produces the right errors, write them to a log file
				StringBuffer errors = new StringBuffer();

				for (SemanticAnalysisException error : semanticResult.getExceptions()) {
					errors.append(error.toString() + "\n");
				}
				TestUtils.writeToFile(expectedResultFilePath.toFile().getPath() + ".errors", errors);
			}

			if (expectedNumberOfErrors > 0) {
				Assert.assertEquals("wrong number of errors", expectedNumberOfErrors, semanticResult.getNumberOfExceptions());
			}
		} else {
			if (parsingError != null || semanticResult.hasErrors()) {
				System.out.println("");
				System.out.println("----------------------------------------------------------------------------");
				System.err.println("Test for file = " + sourceFilePath + " failed");
				if (parsingError != null) {
					parsingError.printParserExceptions();
				}
				for (SemanticAnalysisException error : semanticResult.getExceptions()) {
					error.printStackTrace();
				}
				System.out.println("----------------------------------------------------------------------------");
				System.out.println("");
				Assert.fail("semantic analysis failed on correct program: " + sourceFilePath);
			}
		}

		System.out.println("* " + sourceFilePath + " has passed the test");

	}
}
