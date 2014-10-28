package compiler.lexer;

import compiler.StringTable;

import java.io.BufferedInputStream;

/**
 * Prints lexer output to system out
 *
 * TODO rename me
 */
public class LexerOutputWriter {

    public static void lexSource(BufferedInputStream is, StringTable stringTable) throws Exception {
        Lexer lexer = new Lexer(is, stringTable);
        Token token;

        do
        {
            token = lexer.getNextToken();
            System.out.println(token);
            // TODO what to do if lexer throws an exception?
        } while (token.getType() != TokenType.EOF);
    }

}
