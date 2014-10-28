package compiler;

import compiler.lexer.LexerOutputWriter;

import java.io.*;
import java.nio.file.Path;

/**
 * Facade to the compiler
 * <p/>
 * TODO remove me - (there is a class java.lang.Compiler, so I have renamed this one)
 */
public class MiniJavaCompiler {

    public enum OutputFormat {LEXER}

    private static OutputFormat outputFormat;

    public static void setOutputFormat(OutputFormat outputFormat) {
        MiniJavaCompiler.outputFormat = outputFormat;
    }

    public static void compile(String sourceFile) {
        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(sourceFile));
            StringTable stringTable = new StringTable();

            if (outputFormat == OutputFormat.LEXER)
            {
                LexerOutputWriter.lexSource(is, stringTable);
                return;
            }

        } catch (FileNotFoundException e) {
            System.err.println("Cannot open source file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

}
