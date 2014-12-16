package compiler.firm.backend;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import compiler.utils.Pair;
import compiler.utils.TestUtils;

public class AssemblyTester {

	public static void main(String[] args) throws Exception {
		Path sourceFilePath = Paths.get("testdata/backend/EmptyMain.java");
		Path assemblyFilePath = Paths.get("assembly-backend.s");

		Pair<Integer, List<String>> res = TestUtils.startCompilerApp("--no-opt", "--assembler", assemblyFilePath.toAbsolutePath().toString(),
				"--compile-firm",
				sourceFilePath.toAbsolutePath().toString());

		for (String line : res.getSecond()) {
			System.out.println(line);
		}

		assertEquals("compiling failed for " + sourceFilePath, 0, res.getFirst().intValue());
	}
}
