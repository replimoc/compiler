package compiler.utils;

import compiler.firm.TempFirmCode;
import firm.*;
import firm.nodes.*;

import java.io.IOException;

/**
 * TODO document me
 */
public class FirmTester {

    public static void main(String[] argss) throws IOException {
        Firm.init();
        System.out.printf("Initialized libFirm Version: %1s.%2s\n",
                Firm.getMinorVersion(),
                Firm.getMajorVersion());

        TempFirmCode.createEmptyMethod();
//        TempFirmCode.createPrintIntGraph(mainEntity);

        for (Graph g : Program.getGraphs()) {
            g.check();
            Dump.dumpGraph(g, "--finished");
        }
    }
}
