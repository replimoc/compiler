package compiler.utils;

import java.io.IOException;

import compiler.firm.FirmUtils;
import compiler.firm.TempFirmCode;

import firm.Dump;
import firm.Graph;
import firm.Program;

/**
 * TODO document me
 */
public class FirmTestsMain {

	public static void main(String[] argss) throws IOException {
		FirmUtils.initFirm();

		TempFirmCode.createStaticEmptyMethod();
		TempFirmCode.createStaticMethodWithParam();
		TempFirmCode.createMethodWithLocalVar();
		TempFirmCode.createPrintIntGraph();

		for (Graph g : Program.getGraphs()) {
			g.check();
			Dump.dumpGraph(g, "--finished");
		}

		FirmUtils.finishFirm();
	}
}
