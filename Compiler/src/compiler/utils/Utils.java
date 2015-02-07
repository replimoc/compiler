package compiler.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

public class Utils {

	public static final int DEFAULT_STACK_SIZE_MB = 20;

	private Utils() {
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static Pair<Integer, List<String>> systemExec(List<String> strings) throws IOException {
		return runProcessBuilder(new ProcessBuilder(strings));
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
		return fileName + getBinaryFileEnding();
	}

	public static String getBinaryFileEnding() {
		return isWindows() ? ".exe" : ".out";
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

	public static String createAutoDeleteTempFile(String prefix, String suffix) throws IOException {
		Path standardLibOFile = Files.createTempFile(prefix, suffix);
		standardLibOFile.toFile().deleteOnExit();
		return standardLibOFile.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> Set<T> unionSet(T[]... inputs) {
		Set<T> result = new HashSet<>();
		for (T[] curr : inputs) {
			if (curr == null)
				continue;

			for (int i = 0; i < curr.length; i++) {
				result.add(curr[i]);
			}
		}
		return result;
	}

	@SafeVarargs
	public static <T> Set<T> unionSet(Set<T>... inputs) {
		Set<T> result = new HashSet<>();
		for (Collection<T> curr : inputs) {
			result.addAll(curr);
		}
		return result;
	}

	@SafeVarargs
	public static <T> Set<T> unionSet(T... inputs) {
		Set<T> result = new HashSet<>();
		for (int i = 0; i < inputs.length; i++) {
			result.add(inputs[i]);
		}
		return result;
	}

	public static <T> void cutSet(Set<T> result, Set<T> secondSet) {
		for (Iterator<T> iterator = result.iterator(); iterator.hasNext();) {
			if (!secondSet.contains(iterator.next())) {
				iterator.remove();
			}
		}
	}
}
