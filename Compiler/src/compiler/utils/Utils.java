package compiler.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {

	private Utils() {
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static Pair<Integer, List<String>> systemExec(String... strings) throws IOException {
		return runProcessBuilder(new ProcessBuilder(strings));
	}

	public static Pair<Integer, List<String>> runProcessBuilder(ProcessBuilder processBuilder) throws IOException {
		processBuilder.redirectErrorStream(true);
		try {
			Process process = processBuilder.start();
			List<String> output = readOutput(process.getInputStream());
			int exitValue = process.waitFor();

			return new Pair<>(exitValue, output);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new Pair<>(-1, null);
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

	public static String getBinaryFileName(String fileName) {
		if (isWindows()) {
			return fileName + ".exe";
		} else {
			return fileName + ".out";
		}
	}
}
