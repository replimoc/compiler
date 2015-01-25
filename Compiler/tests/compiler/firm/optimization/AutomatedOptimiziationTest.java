package compiler.firm.optimization;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import compiler.utils.Pair;
import compiler.utils.TestFileVisitor;
import compiler.utils.TestFileVisitor.FileTester;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

public class AutomatedOptimiziationTest implements FileTester {

	private static final String OUTPUT_FILE_EXTENSION = ".result";

	@Test
	public void testConstantFolding() throws Exception {
		TestFileVisitor.runTests(this, "testdata/optimization/constantFolding", TestFileVisitor.JAVA_EXTENSION, OUTPUT_FILE_EXTENSION);
		// uncomment for testing a single file: for debugging purposes
		// testSourceFile(Paths.get("testdata/EmptyMain.java"), null);
	}

	@Override
	public void testSourceFile(TestFileVisitor visitor, Path sourceFilePath, Path expectedResultFilePath) throws Exception {
		System.out.println("Starting optimiziation test for " + sourceFilePath);

		// create a temp file for the created binary
		String binaryFile = Utils.createAutoDeleteTempFile("a", Utils.getBinaryFileEnding());
		// optimized binary
		String optimizedAssemblerFile = Utils.createAutoDeleteTempFile("assembler", ".s");
		Pair<Integer, List<String>> resOptExes = TestUtils.startCompilerApp("--assembler", optimizedAssemblerFile.toString(), "-o", binaryFile,
				sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resOptExes.getFirst().intValue());

		// non optimized binary
		String nonOptimizedAssemblerFile = Utils.createAutoDeleteTempFile("assembler", ".s");
		Pair<Integer, List<String>> resNonOptExe = TestUtils.startCompilerApp("--assembler", nonOptimizedAssemblerFile.toString(), "--no-opt", "-o",
				binaryFile, sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());

		// Optimized binary is greater than non-opt
		long diffSize = nonOptimizedAssemblerFile.length() - optimizedAssemblerFile.length();
		assert (diffSize >= 0);
		if (diffSize > 0) {
			System.out.println(sourceFilePath + ": Optimization applied: optimized version is " + diffSize + " bytes smaller");
		} else {
			System.out.println(sourceFilePath + ": No optimization applied");
		}
	}
}
