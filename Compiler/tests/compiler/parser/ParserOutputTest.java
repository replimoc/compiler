package compiler.parser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import compiler.utils.TestFileVisitor;
import org.junit.Assert;
import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * Test case for correct output of compiler with only parse phase
 * <p/>
 * this test should be started from Compiler directory
 */
public class ParserOutputTest implements TestFileVisitor.FileTester {

    private static final String PARSER_EXTENSION = ".parser";

    @Test
    public void testParserFiles() throws Exception {
        Path testDir = Paths.get("testdata");
        TestFileVisitor parserTester = new TestFileVisitor(PARSER_EXTENSION, this);
        Files.walkFileTree(testDir, parserTester);
        parserTester.checkForFailedTests();
    }

    public void testSourceFile(Path sourceFile, Path parserFile) throws Exception {
        System.out.println("Testing parsing of " + sourceFile);
        // read expected output
        List<String> expectedOutput = Files.readAllLines(parserFile, StandardCharsets.US_ASCII);
        Parser parser = TestUtils.initParser(sourceFile);

        boolean isGrammarValid = false;

        if (expectedOutput.get(0).equals("1")) {
            isGrammarValid = true;
        } else if (expectedOutput.get(0).equals("0")) {
            isGrammarValid = false;
        } else {
            Assert.fail("Invalid content in " + parserFile + ", should contain 1 or 0");
        }

        int errors = parser.parse();
        if (!isGrammarValid && errors == 0) {
            Assert.fail("Parser successful on file " + sourceFile + " which is syntactically incorrect");
        }
        if (isGrammarValid && errors > 0) {
            System.err.println();
            System.err.println("============================= + " + sourceFile + " + =======================================");
            System.err.println("============================= - " + sourceFile + " - =======================================");
            System.err.println();
            Assert.fail("Parser failed on file " + sourceFile + " which is syntactically correct");
        }
    }
}
