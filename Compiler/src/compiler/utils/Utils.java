package compiler.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadFactory;

public class Utils {

	public static final int DEFAULT_STACK_SIZE_MB = 20;

	private Utils() {
	}

	public static boolean isWindows() {
		return false;
		// return System.getProperty("os.name").startsWith("Windows");
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
		return fileName + (isWindows() ? ".exe" : ".out");
	}

	public static String getJarLocation() {
		try {
			File file = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			return file.getParent();
		} catch (URISyntaxException e) {
		}
		return "";
	}

	public static ThreadFactory getThreadFactory(final int stackSizeMB) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(null, r, "runner", stackSizeMB * 1024 * 1024);
			}
		};
	}
}
