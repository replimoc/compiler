package compiler.firm;

import firm.Firm;

public final class FirmUtils {

	private FirmUtils() { // no objects of this class shall be created
	}

	public static void initFirm() {
		System.setProperty("jna.library.path", "lib/firm/");
		Firm.init();

		System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
