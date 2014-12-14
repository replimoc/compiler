package compiler.firm.optimization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import compiler.utils.Pair;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestFileVisitor.FileTester;
import compiler.utils.TestUtils;

public class AutomatedOptimiziationTest implements FileTester {

	private static final String OUTPUT_FILE_EXTENSION = ".result";

	@Test
	public void testConstantFolding() throws Exception {
		TestFileVisitor.runTests(this, "testdata/optimization/constantFolding", TestFileVisitor.JAVA_EXTENSION, OUTPUT_FILE_EXTENSION);
		// uncomment for testing a single file: for debugging purposes
		// testSourceFile(Paths.get("testdata/EmptyMain.java"), null);
	}

	@Override
	public void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception {
		System.out.println("Starting optimiziation test for " + sourceFilePath);

		// optimized binary
		File optExe = File.createTempFile("assembler", ".s");
		optExe.deleteOnExit();
		Pair<Integer, List<String>> resOptExes = TestUtils.startCompilerApp("-o", optExe.toString(), "--assembler",
				sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resOptExes.getFirst().intValue());

		// non optimized binary
		File nonOptExe = File.createTempFile("assembler", ".s");
		nonOptExe.deleteOnExit();
		Pair<Integer, List<String>> resNonOptExe = TestUtils.startCompilerApp("-o", nonOptExe.toString(), "--no-opt",
				"--assembler", sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());

		// Optimized binary is greater than non-opt
		long diffSize = nonOptExe.length() - optExe.length();
		assert (diffSize >= 0);
		if (diffSize > 0) {
			System.out.println(sourceFilePath + ": Optimization applied: optimized version is " + diffSize + " bytes smaller");
		} else {
			System.out.println(sourceFilePath + ": No optimization applied");
		}
	}
}
