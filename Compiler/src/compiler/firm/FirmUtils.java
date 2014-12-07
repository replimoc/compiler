package compiler.firm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import compiler.utils.Pair;
import compiler.utils.Utils;

import firm.Backend;
import firm.ClassType;
import firm.Dump;
import firm.Entity;
import firm.Firm;
import firm.Graph;
import firm.MethodType;
import firm.Program;
import firm.Type;
import firm.Util;

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

		// System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void highToLowLevel() {
		for (Type type : Program.getTypes()) {
			if (type instanceof ClassType) {
				layoutClass((ClassType) type);
			}
			// FIXME It fails on finishLayout(). Why?
			// type.finishLayout();
		}

		for (Entity entity : Program.getGlobalType().getMembers()) {
			entity.setLdIdent(entity.getLdName().replaceAll("[()\\[\\];]", "_"));
		}
		Util.lowerSels();
	}

	private static void layoutClass(ClassType cls) {
		if (cls.equals(Program.getGlobalType()))
			return;

		for (int m = 0; m < cls.getNMembers(); /* nothing */) {
			Entity member = cls.getMember(m);
			Type type = member.getType();
			if (!(type instanceof MethodType)) {
				++m;
				continue;
			}

			/* methods get implemented outside the class, move the entity */
			member.setOwner(Program.getGlobalType());
		}

		cls.layoutFields();
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
	public static String createBinary(String outputFileName, boolean keepAssembler) throws IOException {
		String base = Utils.getJarLocation() + File.separator;
		File assembler = new File("assembler.s");
		if (!keepAssembler) {
			assembler = File.createTempFile("assembler", ".s");
			assembler.deleteOnExit();
		}
		File build = File.createTempFile("build", ".o");
		build.deleteOnExit();

		String standardlibO = base + "resources/standardlib.o";
		new File(standardlibO).deleteOnExit();

		String assemblerFile = assembler.getAbsolutePath();
		String buildFile = build.getAbsolutePath();

		createAssembler(assemblerFile);

		outputFileName += Utils.isWindows() ? ".exe" : ".out";

		printOutput(Utils.systemExec("gcc", "-c", assemblerFile, "-o", buildFile));
		printOutput(Utils.systemExec("gcc", "-c", base + "resources/standardlib.c", "-o", standardlibO));
		printOutput(Utils.systemExec("gcc", "-o", outputFileName, buildFile, standardlibO));

		return outputFileName;
	}

	private static void printOutput(Pair<Integer, List<String>> executionState) {
		if (!executionState.getSecond().isEmpty())
			System.out.println("Exit code: " + executionState.getFirst());

		for (String line : executionState.getSecond()) {
			System.out.println(line);
		}
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
