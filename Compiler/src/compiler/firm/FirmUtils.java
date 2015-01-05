package compiler.firm;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import compiler.utils.ExecutionFailedException;
import compiler.utils.Pair;
import compiler.utils.Utils;

import firm.Backend;
import firm.ClassType;
import firm.Dump;
import firm.Entity;
import firm.Firm;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Program;
import firm.Type;
import firm.Util;

public final class FirmUtils {

	private static final String JNA_LIBRARY_PATH = "jna.library.path";
	private static final String LIB_FIRM_FOLDER = "lib/firm/";
	private static final String ISA_AMD64 = "isa=amd64";

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	private FirmUtils() { // no objects of this class shall be created
	}

	public static void initFirm() {
		if (System.getProperty(JNA_LIBRARY_PATH) == null) {
			System.setProperty(JNA_LIBRARY_PATH, Utils.getJarLocation() + File.separator + LIB_FIRM_FOLDER);
		}
		Firm.init();

		// System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void highToLowLevel() {
		for (Type type : Program.getTypes()) {
			if (type instanceof ClassType) {
				layoutClass((ClassType) type);
			}
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

	public interface AssemblerCreator {
		public void create(String file) throws IOException;
	}

	/**
	 * Expect escaped outputFileName.
	 * 
	 * @param outputFileName
	 *            File for the binary executable.
	 * @throws IOException
	 * @throws ExecutionFailedException
	 */
	public static void createBinary(String outputFileName, String assemblerFile, AssemblerCreator assemblerCreator, String cInclude, String cLibrary)
			throws IOException,
			ExecutionFailedException {
		String base = Utils.getJarLocation() + File.separator;
		if (assemblerFile == null) {
			assemblerFile = Utils.createAutoDeleteTempFile("assembler", ".s");
		}

		assemblerCreator.create(assemblerFile);

		List<String> execOptions = new LinkedList<String>();
		execOptions.addAll(Arrays.asList("gcc", "-o", outputFileName));
		execOptions.add(compileToO(assemblerFile, "build"));
		execOptions.add(compileToO(base + "resources/standardlib.c", "standardlib"));

		if (cInclude != null)
			execOptions.add(compileToO(cInclude, "cInclude"));

		if (cLibrary != null)
			execOptions.add("-l" + cLibrary);

		printOutput(Utils.systemExec(execOptions));
	}

	private static String compileToO(String inputFile, String outputFileName) throws IOException, ExecutionFailedException {
		String standardlibO = Utils.createAutoDeleteTempFile(outputFileName, ".o");
		printOutput(Utils.systemExec("gcc", "-c", inputFile, "-o", standardlibO));
		return standardlibO;
	}

	private static void printOutput(Pair<Integer, List<String>> executionState) throws ExecutionFailedException {
		for (String line : executionState.getSecond()) {
			System.out.println(line);
		}

		int exitCode = executionState.getFirst();
		if (exitCode != 0) {
			throw new ExecutionFailedException(exitCode);
		}
	}

	public static void createFirmGraph(String suffix) {
		for (Graph graph : Program.getGraphs()) {
			graph.check();
			Dump.dumpGraph(graph, suffix.isEmpty() ? "generated" : suffix);
		}
	}

	/**
	 * Returns mode of 32-bit integer signed
	 * 
	 * @return
	 */
	public static Mode getModeInteger() {
		return Mode.getIs();
	}

	/**
	 * Returns mode for 8-bit boolean.
	 * 
	 * @return
	 */
	public static Mode getModeBoolean() {
		return Mode.getBu();
	}

	/**
	 * Returns reference mode for 64-bit
	 * 
	 * @return
	 */
	public static Mode getModeReference() {
		return Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64);
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
