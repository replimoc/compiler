package compiler.semantic;

import compiler.StringTable;
import compiler.ast.AstNode;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.utils.TestFileVisitor;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test correctness of semantic analysis
 * che
 */
public class CheckOutputTest implements TestFileVisitor.FileTester {

    private static final String SEMANTIC_CHECK_EXTENSION = ".sc";

    @Test
    public void testCheckFiles() throws Exception {
        Path testDir = Paths.get("testdata");
        TestFileVisitor lexTester = new TestFileVisitor(SEMANTIC_CHECK_EXTENSION, this);
        Files.walkFileTree(testDir, lexTester);
        lexTester.checkForFailedTests();
    }

    @Override
    public void testSourceFile(Path sourceFilePath, Path expectedResultFilePath) throws Exception {

        // read expected results file
        List<String> lines = Files.readAllLines(expectedResultFilePath, StandardCharsets.US_ASCII);
        boolean isErrorExpected = !"correct".equals(lines.get(0));

        // start lexer
        Lexer lexer = new Lexer(Files.newBufferedReader(sourceFilePath, StandardCharsets.US_ASCII), new StringTable());
        Parser parser = new Parser(lexer);

        List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(parser.parse());
        if (isErrorExpected) {
            if(errors.size() == 0) Assert.fail("semantic analysis succeeded on incorrect program: " + sourceFilePath);
        } else {
            if (!errors.isEmpty()) {
                System.err.println("");
                System.err.println("----------------------------------------------------------------------------");
                System.err.println("Test for file = " + sourceFilePath + " failed");
                for (SemanticAnalysisException error : errors) {
                    error.printStackTrace();
                }
                System.err.println("----------------------------------------------------------------------------");
                System.err.println("");
            }
            Assert.fail("semantic analysis failed on correct program: " + sourceFilePath);
        }

    }
}
