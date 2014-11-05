package compiler.parser;

import compiler.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case for correct output of compiler with only parse phase
 * <p/>
 * this test should be started from Compiler directory
 */
public class ParserOutputTest {

    public static class ParseTester extends SimpleFileVisitor<Path> {
        private static final String PARSER_EXTENSION = ".parser";
        private static final String JAVA_EXTENSION = ".java";

        private final PathMatcher matcher;
        private List<Path> failedTestsList = new ArrayList<>();

        public ParseTester() {
            matcher = FileSystems.getDefault().getPathMatcher("glob:*" + PARSER_EXTENSION);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                String parseFilename = name.toString();
                String sourceFilename = parseFilename.replace(PARSER_EXTENSION, JAVA_EXTENSION);

                Path sourceFilePath = file.getParent().resolve(sourceFilename);

                if (!Files.exists(sourceFilePath)) {
                    Assert.fail("cannot find program to output " + sourceFilePath);
                }

                try {
                    testSourceFile(sourceFilePath, file);
                } catch (Exception e) {
                    System.err.println("Test for file = " + file + " failed");
                    e.printStackTrace();
                    failedTestsList.add(file);
                } catch (AssertionError e) {
                    System.err.println("Test for file = " + file + " failed");
                    e.printStackTrace();
                    failedTestsList.add(file);
                }

            }
            return FileVisitResult.CONTINUE;
        }

        public void checkForFailedTests() {
            if (failedTestsList.size() != 0) {
                Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
            }
        }
    }

    @Test
    public void testParserFiles() throws Exception {
        Path testDir = Paths.get("testdata");
        ParseTester parserTester = new ParseTester();
        Files.walkFileTree(testDir, parserTester);
        parserTester.checkForFailedTests();
    }

    private static void testSourceFile(Path sourceFile, Path parserFile) throws Exception {
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

        try {
            parser.parse();
            if(!isGrammarValid)
            {
                Assert.fail("Parser successful on file " + sourceFile + " which is syntactically incorrect");
            }
        } catch (ParserException pe) {
            if(isGrammarValid)
            {
                System.err.println();
                System.err.println("============================= + " + sourceFile + " + =======================================");
                pe.printStackTrace();
                System.err.println("============================= - " + sourceFile + " - =======================================");
                System.err.println();
                Assert.fail("Parser failed on file " + sourceFile + " which is syntactically correct");
            }
        }
    }
}
