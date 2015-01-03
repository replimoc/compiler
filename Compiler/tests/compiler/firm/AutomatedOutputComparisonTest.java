package compiler.firm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private static final String OUTPUT_FILE_EXTENSION_MJTEST = ".check";

	@Before
	public void initFirm() {
		FirmUtils.initFirm();
	}

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

	@After
	public void finishFirm() {
		FirmUtils.finishFirm();
	}

	@Override
	public void testSourceFile(Path sourceFile, Path expectedFile, Path cIncludeFilePath) throws Exception {
		System.out.println("Testing execution result of " + sourceFile);
		File binarFile = File.createTempFile("executable", Utils.getBinaryFileName(""));
		binarFile.deleteOnExit();

		Pair<Integer, List<String>> compilingState;
		if (Files.exists(cIncludeFilePath)) {
			compilingState = TestUtils.startCompilerApp(getAdditionalOptions(),
					"-o", binarFile.toString(), "--c-include",
					cIncludeFilePath.toString(),
					sourceFile.toAbsolutePath().toString());
		} else {
			compilingState = TestUtils.startCompilerApp(getAdditionalOptions(),
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

	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm" };
	}
}
