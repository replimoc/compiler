package compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Utils {

	private Utils() {
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static List<String> systemExec(String... strings) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(strings);
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();
			process.waitFor();

			return readOutput(process.getInputStream());
		} catch (InterruptedException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private static List<String> readOutput(InputStream in) {
		List<String> outputLines = new ArrayList<String>();
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) {
			outputLines.add(s.nextLine());
		}
		s.close();
		return outputLines;
	}
}
