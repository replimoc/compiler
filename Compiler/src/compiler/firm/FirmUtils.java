package compiler.firm;

import firm.Firm;

public class FirmUtils {

	public static void initFirm() {
		System.setProperty("jna.library.path", "lib/firm/");
		Firm.init();

		System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void finishFirm() {
		Firm.finish();
	}
}
