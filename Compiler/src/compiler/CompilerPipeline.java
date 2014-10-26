package compiler;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO document me
 */
public class CompilerPipeline {

    private String sourceFileName;
    private boolean debug;

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public void setDebugOn() {
        this.debug = true;
    }

    public void compile() throws Exception {
        if (debug) System.out.println("Compiling source file " + sourceFileName);
//        if(debug) System.out.println("Working Directory = " + System.getProperty("user.dir"));

        Path sourceFile = Paths.get(sourceFileName);
        if (!Files.exists(sourceFile) && !Files.isRegularFile(sourceFile)) {
            throw new Exception("file " + sourceFileName + " doesn't exist!");
        } else if (!Files.isReadable(sourceFile)) {
            throw new Exception("file " + sourceFileName + " is not readable");
        }

        byte[] sourceBytes = Files.readAllBytes(sourceFile);

        // void* tokens = Lexer::process(sourceBytes);
        // void* ... = Parser::process(tokens);
        // ...

        byte[] destBytes = sourceBytes;

        // TODO do we do a.out ( + -o output.file) or (filename).asm?
        Path destFile = Paths.get("a.out");
        if (Files.exists(destFile)) {
            Files.write(destFile, destBytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            Files.write(destFile, destBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        }
    }
}
