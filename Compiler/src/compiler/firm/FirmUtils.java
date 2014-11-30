package compiler.firm;

import java.io.IOException;

import firm.Backend;
import firm.Firm;

public final class FirmUtils {

	private static final String JNA_LIBRARY_PATH = "jna.library.path";
	private static final String LIB_FIRM_FOLDER = "lib/firm/";

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
		Backend.createAssembler(outputFileName, "<builtin>");
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
