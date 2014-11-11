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

import compiler.utils.TestFileVisitor;
import org.junit.Assert;
import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * Test case for correct output of compiler with --lextest option
 * <p/>
 * this test should be started from Compiler directory
 */
public class LextestOutputTest implements TestFileVisitor.FileTester  {

    private static final String LEXER_EXTENSION = ".lexer";

	@Test
	public void testLexerFiles() throws Exception {
		Path testDir = Paths.get("testdata");
        TestFileVisitor lexTester = new TestFileVisitor(LEXER_EXTENSION, this);
		Files.walkFileTree(testDir, lexTester);
		lexTester.checkForFailedTests();
	}

	public void testSourceFile(Path sourceFile, Path lexFile) throws Exception {
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
