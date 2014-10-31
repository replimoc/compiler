package compiler.lexer;

import compiler.StringTable;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.nio.file.FileVisitResult.*;

/**
 * Test case for correct output of compiler with --lextest option
 * <p/>
 * this test should be started from Compiler directory
 */
public class LextestOutputTest {

    HashSet<String> excludedSourceFiles = new HashSet<String>();

    public static class LexTester extends SimpleFileVisitor<Path> {
        private static final String lexerExtension = ".lexer";
        private static final String javaExtension = ".java";

        private final PathMatcher matcher;
        private List<Path> failedTestsList = new ArrayList<>();

        public LexTester() {
            matcher = FileSystems.getDefault().getPathMatcher("glob:*" + lexerExtension);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                String lexFilename = name.toString();
                String sourceFilename = lexFilename.replace(lexerExtension, javaExtension);

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
            return CONTINUE;
        }

        public void checkForFailedTests() {
            if (failedTestsList.size() != 0) {
                Assert.fail("Tests for " + failedTestsList.size() + " test(s) failed");
            }
        }
    }


    @Before
    public void setUp() throws Exception {
//        excludedSourceFiles.add("empty.lexer");
    }

    @Test
    public void testLexerFiles() throws Exception {
        Path testDir = Paths.get("testdata");
        LexTester lexTester = new LexTester();
        Files.walkFileTree(testDir, lexTester);
        lexTester.checkForFailedTests();
    }

    private static void testSourceFile(Path sourceFile, Path lexFile) throws Exception {
        // read expected output
        BufferedReader expectedOutput = Files.newBufferedReader(lexFile, StandardCharsets.US_ASCII);
        BufferedInputStream sourceIs = new BufferedInputStream(Files.newInputStream(sourceFile));

        Lexer lexer = new Lexer(sourceIs, new StringTable());

        // compare expected output and actual output line by line
        String expectedLine;
        String actualLine;

        while ((expectedLine = expectedOutput.readLine()) != null) {
            Token nextToken = lexer.getNextToken();
            Assert.assertNotNull("missing output: expected " + expectedLine, nextToken);
            actualLine = nextToken.getTokenString();
//            System.out.println(actualLine);
            Assert.assertEquals(expectedLine, actualLine);
        }

        Token nextToken = lexer.getNextToken();
        Assert.assertNull("not expected output, expected eof", nextToken);
    }

    /**
     * This test takes all expected outputs in $PROJECT_DIR/testdata/lextest-out, finds corresponding sources in
     * $PROJECT_DIR/testdata/sources,runs compiler with --lextest and sourceFile, and then compares output line by line.
     * <p/>
     * This test requires project jar file in target directory
     * This test can only be run on linux (it calls script ./compiler.sh)
     * This test must be run from working directory $PROJECT_DIR = compiler/Compiler
     * (and I don't know how to fix this)
     * <p/>
     * ...so it must be called manually
     * <p/>
     * to exclude expected results from testing add them to excludedSourceFiles
     */
    @Test
    @Ignore
    public void testFiles() throws Exception {

        // read files in lextest-out
        Path lextestDir = Paths.get("testdata/lextest-out");
        DirectoryStream<Path> stream = Files.newDirectoryStream(lextestDir);

        for (Path lextestFile : stream) {
            // find corresponding program in testdata/sources
            String filename = lextestFile.getFileName().toString();
            if (excludedSourceFiles.contains(filename)) continue;

            String programFilename = "testdata/sources/" + filename.replace(".lexer", ".java");

            if (!Files.exists(Paths.get(programFilename))) {
                Assert.fail("cannot find program to output " + filename);
            }

            System.out.println("===== " + programFilename + " =====");

            // run lexer and get output
            ProcessBuilder pb = new ProcessBuilder("./compiler.sh", "--lextest", programFilename);
            pb.redirectErrorStream(true);
            Process compiler = pb.start();
            BufferedReader compilerOutput = new BufferedReader(new InputStreamReader(compiler.getInputStream()));

            // read expected output
            BufferedReader expectedOutput = Files.newBufferedReader(lextestFile, StandardCharsets.US_ASCII);

            // compare expected output and actual output line by line
            String expectedLine;
            String outputLine;

            while ((expectedLine = expectedOutput.readLine()) != null) {
                outputLine = compilerOutput.readLine();
                System.out.println(outputLine);
                Assert.assertNotNull("missing output: expected " + outputLine, outputLine);
                Assert.assertEquals(expectedLine, outputLine);
            }

            outputLine = compilerOutput.readLine();
            Assert.assertNull("not expected output, expected eof", outputLine);

        }

    }
}
