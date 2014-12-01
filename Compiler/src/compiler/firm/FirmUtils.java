package compiler.firm;

import java.io.File;
import java.io.IOException;

import compiler.Utils;

import firm.Backend;
import firm.Dump;
import firm.Firm;
import firm.Graph;
import firm.Program;

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
	 * @return
	 * @throws IOException
	 */
	public static String createBinary(String outputFileName) throws IOException {
		File assembler = File.createTempFile("assembler", ".s");
		assembler.deleteOnExit();
		File build = File.createTempFile("build", ".o");
		build.deleteOnExit();

		String assemblerFile = assembler.getAbsolutePath();
		String buildFile = build.getAbsolutePath();

		createAssembler(assemblerFile);

		if (Utils.isWindows()) {
			outputFileName += ".exe";
		} else {
			outputFileName += ".out";
		}

		Utils.systemExec("gcc", "-c", assemblerFile, "-o", buildFile);
		Utils.systemExec("gcc", "-c", "resources/print_int.c", "-o", "resources/print_int.o");
		Utils.systemExec("gcc", "-o", outputFileName, buildFile, "resources/print_int.o");

		return outputFileName;
	}

	public static void createFirmGraph() {
		for (Graph graph : Program.getGraphs()) {
			graph.check();
			Dump.dumpGraph(graph, "generated");
		}
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
