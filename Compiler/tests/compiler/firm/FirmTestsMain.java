package compiler.firm;

import java.io.IOException;

import org.junit.Ignore;

import firm.Dump;
import firm.Graph;
import firm.Program;

/**
 * TODO document me
 */
@Ignore
public class FirmTestsMain {

	public static void main(String[] args) throws IOException {
		FirmUtils.initFirm();

		// TempFirmCode.createStaticEmptyMethod();
		// TempFirmCode.createStaticMethodWithParam();
		// TempFirmCode.createMethodWithLocalVar();
		// TempFirmCode.createMethodWithReferenceToClass();
		// TempFirmCode.createPrintIntGraph();
		TempFirmCode.createCallocGraph();

		for (Graph g : Program.getGraphs()) {
			g.check();
			Dump.dumpGraph(g, "--finished");
		}

		FirmUtils.finishFirm();
	}
}
