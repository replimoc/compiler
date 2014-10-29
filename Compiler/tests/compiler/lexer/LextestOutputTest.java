package compiler.lexer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Test case for correct output of compiler with --lextest option
 * <p/>
 * this test should be started from Compiler directory
 */
public class LextestOutputTest {

    HashSet<String> excludedSourceFiles = new HashSet<String>();

    @Before
    public void setUp() throws Exception {
//        excludedSourceFiles.add("empty.lexer");
    }

    /**
     * This test takes all expected outputs in $PROJECT_DIR/testdata/lextest-out, finds corresponding sources in
     * $PROJECT_DIR/testdata/sources,runs compiler with --lextest and sourceFile, and then compares output line by line.
     *
     * This test requires project jar file in target directory
     * This test can only be run on linux (it calls script ./compiler.sh)
     * This test must be run from working directory $PROJECT_DIR = compiler/Compiler
     *    (and I don't know how to fix this)
     *
     * ...so it must be called manually
     *
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
            if(excludedSourceFiles.contains(filename)) continue;

            String programFilename = "testdata/sources/" + filename.replace(".lexer", ".java");

            if (!Files.exists(Paths.get(programFilename))) {
                Assert.fail("cannot find program to output " + filename);
            }

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
                Assert.assertNotNull("missing output: expected " + outputLine,outputLine);
                Assert.assertEquals(expectedLine, outputLine);
            }

            outputLine = compilerOutput.readLine();
            Assert.assertNull("not expected output, expected eof", outputLine);

        }

    }
}
