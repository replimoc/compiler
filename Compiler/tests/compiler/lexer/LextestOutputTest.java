package compiler.lexer;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * Test case for correct output of compiler with --lextest option
 * <p/>
 * this test should be started from Compiler directory
 */
public class LextestOutputTest {

	public static class LexTester extends SimpleFileVisitor<Path> {
		private static final String LEXER_EXTENSION = ".lexer";
		private static final String JAVA_EXTENSION = ".java";

		private final PathMatcher matcher;
		private List<Path> failedTestsList = new ArrayList<>();

		public LexTester() {
			matcher = FileSystems.getDefault().getPathMatcher("glob:*" + LEXER_EXTENSION);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				String lexFilename = name.toString();
				String sourceFilename = lexFilename.replace(LEXER_EXTENSION, JAVA_EXTENSION);

				Path sourceFilePath = file.getParent().resolve(sourceFilename);

				if (!Files.exists(sourceFilePath)) {
					Assert.fail("cannot find program to output " + sourceFilePath);
				}

				try {
					testSourceFile(sourceFilePath, file);
				} catch (Exception e) {
					System.err.println("Test for file = " + file + " failed");
					e.printStackTrace();
					failedTestsList.add(file);
				} catch (AssertionError e) {
					System.err.println("Test for file = " + file + " failed");
					e.printStackTrace();
					failedTestsList.add(file);
				}

			}
			return FileVisitResult.CONTINUE;
		}

		public void checkForFailedTests() {
			if (failedTestsList.size() != 0) {
				Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
			}
		}
	}

	@Test
	public void testLexerFiles() throws Exception {
		Path testDir = Paths.get("testdata");
		LexTester lexTester = new LexTester();
		Files.walkFileTree(testDir, lexTester);
		lexTester.checkForFailedTests();
	}

	private static void testSourceFile(Path sourceFile, Path lexFile) throws Exception {
		// read expected output
		BufferedReader expectedOutput = Files.newBufferedReader(lexFile, StandardCharsets.US_ASCII);

		Lexer lexer = TestUtils.initLexer(sourceFile);

		// compare expected output and actual output line by line
		String expectedLine;
		int lineNumber = 0;

		while ((expectedLine = expectedOutput.readLine()) != null) {
			lineNumber++;
			Token nextToken = lexer.getNextToken();
			Assert.assertNotNull("missing output: expected " + expectedLine + " in line " + lineNumber, nextToken);
			Assert.assertEquals("tokens not equal in line in line " + lineNumber, expectedLine, nextToken.getTokenString());
		}

		Token nextToken = lexer.getNextToken();
		Assert.assertNull("not expected output, expected null after file", nextToken);
	}
}
