package compiler.firm;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compiler.utils.Pair;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

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
	public void testCompareOutputWithReference() throws Exception {
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

		Pair<Integer, List<String>> compilingState = TestUtils.startCompilerApp("--compile-firm", sourceFile.toAbsolutePath().toString());
		for (String line : compilingState.getSecond()) {
			System.out.println(line);
		}
		assertEquals("compiling failed for " + sourceFile, 0, compilingState.getFirst().intValue());

		Pair<Integer, List<String>> executionState = Utils.systemExec(Utils.getBinaryFileName("./a"));
		// assertEquals("execution of assembly failed for " + sourceFile, 0, executionState.getFirst().intValue());

		List<String> expectedOutput = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
		TestUtils.assertLinesEqual(sourceFile, expectedOutput, executionState.getSecond().iterator());

		System.out.println(sourceFile + " passed ------------------------------------------------------------------------\n");
	}
}
