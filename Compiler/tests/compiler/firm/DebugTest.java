package compiler.firm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import compiler.utils.Pair;
import compiler.utils.TestUtils;
import compiler.utils.Utils;

public class DebugTest {

	@Test
	public void testSourceFile() throws Exception {
		Path sourceFile = Paths.get("testdata/mj-test/run/Simon_146.mj");

		System.out.println("Testing execution result of " + sourceFile);
		File binarFile = File.createTempFile("executable", Utils.getBinaryFileName(""));
		binarFile.deleteOnExit();

		Pair<Integer, List<String>> compilingState;

		compilingState = compile(new String[0], "--assembler=assembler.s", "-o", binarFile.toString(), sourceFile.toAbsolutePath().toString());

		for (String line : compilingState.getSecond()) {
			System.out.println(line);
		}
		assertEquals("compiling failed for " + sourceFile, 0, compilingState.getFirst().intValue());

		System.out.println("DEBUGGING TEST GENERATED THE FOLLOWING ASSEMBLER===============");

		Scanner scanner = new Scanner(Files.newBufferedReader(Paths.get("assembler.s"), StandardCharsets.US_ASCII));
		while (scanner.hasNextLine()) {
			System.out.println(scanner.nextLine());
		}
		scanner.close();

		System.out.println("END OF DEBUGGING TEST==========================================");

	}

	private Pair<Integer, List<String>> compile(String[] args0, String... args1) throws IOException {
		String[] args = new String[args0.length + args1.length];
		System.arraycopy(args0, 0, args, 0, args0.length);
		System.arraycopy(args1, 0, args, args0.length, args1.length);

		return TestUtils.startCompilerApp(args);
	}
}
