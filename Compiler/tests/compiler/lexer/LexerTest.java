package compiler.lexer;

import compiler.StringTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Test case for lexer (check that get_token method outputs correct tokens)
 *
 * @author effenok
 */
public class LexerTest {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testEmpty() {
        String empty = "";
        BufferedInputStream is = getBufferedInputStream(empty);
        try {
            Lexer lexer = new Lexer(is, stringTable);

            Token token = lexer.getNextToken();
            Assert.assertEquals(TokenType.EOF, token.getType());

            // TODO should the next token after EOF be EOF or null or not specified?
            token = lexer.getNextToken();
            Assert.assertEquals(TokenType.EOF, token.getType());

        } catch (IOException e) {
            e.printStackTrace();
            org.junit.Assert.fail();
        }
    }

    private BufferedInputStream getBufferedInputStream(String empty) {
        return new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.US_ASCII)));
    }

}
