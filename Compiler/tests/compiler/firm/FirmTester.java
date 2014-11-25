package compiler.firm;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;
import firm.Dump;
import firm.Graph;
import firm.Program;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * TODO to be firm test
 */
public class FirmTester {

    public static void main(String[] args) throws Exception {

        String filename = "firmdata/test.java";

        Lexer lexer = new Lexer(Files.newBufferedReader(Paths.get(filename), StandardCharsets.US_ASCII), new StringTable());
        Parser parser = new Parser(lexer);
        compiler.ast.Program program = parser.parse();
        List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(program);
        if(errors.size() != 0) {
            for (SemanticAnalysisException error : errors) {
                error.printStackTrace();
            }
            throw new Exception("program is not semantically correct");
        }

        FirmGenerationVisitor firmGen = new FirmGenerationVisitor();
        program.accept(firmGen);

        for (Graph g : Program.getGraphs()) {
            g.check();
            Dump.dumpGraph(g, "--finished");
        }
    }


}
