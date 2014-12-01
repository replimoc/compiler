package compiler;

import java.io.IOException;

public class Utils {

	private Utils() {
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static void systemExec(String... strings) throws IOException {
		Process p = Runtime.getRuntime().exec(strings);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
