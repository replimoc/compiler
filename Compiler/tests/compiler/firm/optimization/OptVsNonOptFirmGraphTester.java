package compiler.firm.optimization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import compiler.utils.Pair;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

public class OptVsNonOptFirmGraphTester {

	public static void main(String[] args) throws Exception {
		testSingleFile(Paths.get("testdata/optimization/constantFolding/Add.java"));
	}

	private static void testSingleFile(Path sourceFilePath) throws IOException {
		System.out.println("Starting generation of non-opt / opt firm graphs for " + sourceFilePath);

		// optimized binary
		File optExe = File.createTempFile("executable", Utils.getBinaryFileName(""));
		optExe.deleteOnExit();
		Pair<Integer, List<String>> resOptExes = TestUtils.startCompilerApp("-s", "optimized", "--graph-firm",
				sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resOptExes.getFirst().intValue());

		// non optimized binary
		File nonOptExe = File.createTempFile("executable", Utils.getBinaryFileName(""));
		nonOptExe.deleteOnExit();
		Pair<Integer, List<String>> resNonOptExe = TestUtils.startCompilerApp("-s", "non-optimized", "--graph-firm", "--no-opt",
				sourceFilePath.toAbsolutePath().toString());

		assertEquals("compiling failed for " + sourceFilePath, 0, resNonOptExe.getFirst().intValue());
	}
}
