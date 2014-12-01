package compiler.firm;

import java.io.File;
import java.io.IOException;

import firm.Backend;
import firm.Firm;

public final class FirmUtils {

	private static final String JNA_LIBRARY_PATH = "jna.library.path";
	private static final String LIB_FIRM_FOLDER = "lib/firm/";
	private static final String ISA_AMD64 = "isa=amd64";

	private FirmUtils() { // no objects of this class shall be created
	}

	public static void initFirm() {
		if (System.getProperty(JNA_LIBRARY_PATH) == null) {
			System.setProperty(JNA_LIBRARY_PATH, LIB_FIRM_FOLDER);
		}
		Firm.init();

		System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void createAssembler(String outputFileName) throws IOException {
		Backend.option(ISA_AMD64);
		Backend.createAssembler(outputFileName, "<builtin>");
	}

	/**
	 * Expect escaped outputFileName.
	 * 
	 * @param outputFileName
	 *            File for the binary executable.
	 * @throws IOException
	 */
	public static void createBinary(String outputFileName) throws IOException {
		File assembler = File.createTempFile("assembler", ".s");
		File build = File.createTempFile("build", ".o");
		String assemblerFile = assembler.getAbsolutePath();
		String buildFile = build.getAbsolutePath();

		System.out.println(assemblerFile);
		createAssembler(assemblerFile);
		String gcc = "gcc";
		if (isWindows()) {
			gcc += ".exe";
		}

		systemExec(new String[] { gcc, "-c", assemblerFile, "-o", buildFile });
		systemExec(new String[] { gcc, "-c", "resources/print_int.c", "-o", "resources/print_int.o" });
		systemExec(new String[] { gcc, "-o", outputFileName, buildFile, "resources/print_int.o" });

		assembler.delete();
		build.delete();
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static void systemExec(String[] strings) throws IOException {
		Process p = Runtime.getRuntime().exec(strings);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
