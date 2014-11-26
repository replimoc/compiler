package compiler.firm;

import firm.Firm;

public class FirmUtils {

	public static void initFirm() {
		System.setProperty("jna.library.path", "lib/firm/");
		Firm.init();

		System.out.printf("Initialized libFirm Version: %1s.%2s\n", Firm.getMinorVersion(), Firm.getMajorVersion());
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
