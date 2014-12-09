package compiler.firm.optimization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compiler.firm.FirmUtils;
import compiler.utils.Pair;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestFileVisitor.FileTester;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

/**
 * This test tries to automatically check whether the compiler successfully has optimized given code.
 *
 */
public class AutomatedOptimizationTest implements FileTester {

	private static final String OUTPUT_FILE_EXTENSION = ".result";

	@Before
	public void initFirm() {
		FirmUtils.initFirm();
	}

	@After
	public void finishFirm() {
		FirmUtils.finishFirm();
	}

	@Test
	public void testOptimization() throws Exception {
		TestFileVisitor.runTests(this, "testdata", TestFileVisitor.JAVA_EXTENSION, OUTPUT_FILE_EXTENSION);
	}

	@Test
	public void testSingleFileForOptimiziation() throws IOException {
		testOptimizationForSingleFile(Paths.get("testdata/outputTest/system.out.println/systemOutPrintln.java"),
				Paths.get("testdata/outputTest/system.out.println/systemOutPrintln.result"));
	}

	@Override
	public void testSourceFile(Path sourceFilePath, Path expectedResultFilePath)
			throws Exception {
		testOptimizationForSingleFile(sourceFilePath, expectedResultFilePath);
	}

	private void testOptimizationForSingleFile(Path sourceFilePath, Path expectedResultFilePath) throws IOException {
		System.out.println("Starting optimiziation test for " + sourceFilePath);

		File nonOptExe = File.createTempFile("executable", Utils.getBinaryFileName(""));
		nonOptExe.deleteOnExit();
		Pair<Integer, List<String>> resNonOptExe = TestUtils.startCompilerApp("-o", nonOptExe.toString(), "--compile-firm --no-opt",
				sourceFilePath.toAbsolutePath().toString());
		assertEquals("compiling failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());

		File optExe = File.createTempFile("executable", Utils.getBinaryFileName(""));
		optExe.deleteOnExit();
		Pair<Integer, List<String>> resOptExes = TestUtils.startCompilerApp("-o", optExe.toString(), "--compile-firm",
				sourceFilePath.toAbsolutePath().toString());
		assertEquals("compiling failed for " + sourceFilePath, 0, resOptExes.getFirst().intValue());

		// execute non optimized
		resNonOptExe = Utils.systemExec(nonOptExe.toString());
		assertEquals("execution of assembly failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());

		// execute optimized
		resOptExes = Utils.systemExec(nonOptExe.toString());
		assertEquals("execution of assembly failed for " + sourceFilePath, 0, resOptExes.getFirst().intValue());

		// check output of non optimized executable against expected
		List<String> expectedOutput = Files.readAllLines(expectedResultFilePath, StandardCharsets.US_ASCII);
		TestUtils.assertLinesEqual(sourceFilePath, expectedOutput, resNonOptExe.getSecond().iterator());

		// check output of optimized executable against expected
		TestUtils.assertLinesEqual(sourceFilePath, expectedOutput, resOptExes.getSecond().iterator());

		// TODO: Implement some comparison

		assertEquals("compiling failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());
	}
}
