package compiler.firm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import compiler.main.CompilerApp;
import compiler.utils.Pair;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

/**
 * Test case for correct output of compiler with only parse phase
 * <p/>
 * this test should be started from Compiler directory
 */
public abstract class AbstractAutomatedOutputComparisonTest implements TestFileVisitor.FileTester {

	private static final String OUTPUT_FILE_EXTENSION = ".result";
	private static final String OUTPUT_FILE_EXTENSION_MJTEST = ".check";

	@Test
	public void testCompareJavaOutputWithResultReference() throws Exception {
		TestFileVisitor.runTests(this, "testdata", TestFileVisitor.JAVA_EXTENSION, OUTPUT_FILE_EXTENSION);
	}

	@Test
	public void testCompareMJOutputWithCheckReference() throws Exception {
		TestFileVisitor.runTests(this, "testdata", TestFileVisitor.MINIJAVA_EXTENSION, OUTPUT_FILE_EXTENSION_MJTEST);
	}

	@Test
	public void testCompileMjTest() throws Exception {
		TestFileVisitor.runTestsForFolder(this, "testdata/mj-test/pos");
	}

	@Override
	public void testSourceFile(Path sourceFile, Path expectedFile, Path cIncludeFilePath) throws Exception {
		System.out.println("Testing execution result of " + sourceFile);
		File binarFile = File.createTempFile("executable", Utils.getBinaryFileName(""));
		binarFile.deleteOnExit();

		Pair<Integer, List<String>> compilingState;
		if (Files.exists(cIncludeFilePath)) {
			compilingState = compile(getAdditionalOptions(),
					"-o", binarFile.toString(), "--c-include",
					cIncludeFilePath.toString(),
					sourceFile.toAbsolutePath().toString());
		} else {
			compilingState = compile(getAdditionalOptions(),
					"-o", binarFile.toString(), sourceFile.toAbsolutePath().toString());
		}

		for (String line : compilingState.getSecond()) {
			System.out.println(line);
		}
		assertEquals("compiling failed for " + sourceFile, 0, compilingState.getFirst().intValue());

		if (!sourceFile.equals(expectedFile)) {
			Pair<Integer, List<String>> executionState = Utils.systemExec(binarFile.toString());
			assertEquals("execution of assembly failed for " + sourceFile, 0, executionState.getFirst().intValue());

			List<String> expectedOutput = Files.readAllLines(expectedFile, StandardCharsets.US_ASCII);
			TestUtils.assertLinesEqual(sourceFile, expectedOutput, executionState.getSecond().iterator());
		}

		System.out.println(sourceFile + " passed ------------------------------------------------------------------------\n");
	}

	private Pair<Integer, List<String>> compile(String[] args0, String... args1) throws IOException {
		String[] args = new String[args0.length + args1.length];
		System.arraycopy(args0, 0, args, 0, args0.length);
		System.arraycopy(args1, 0, args, args0.length, args1.length);

		if (forkProcess()) {
			return TestUtils.startCompilerApp(args);
		} else {
			int exitCode = CompilerApp.execute(args);
			return new Pair<Integer, List<String>>(exitCode, new LinkedList<String>());
		}
	}

	protected abstract String[] getAdditionalOptions();

	protected abstract boolean forkProcess();
}
