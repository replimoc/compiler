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
public class LexerBasicTest {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testEmpty() throws Exception {
        String empty = "";
        BufferedInputStream is = getBufferedInputStream(empty);

        Lexer lexer = new Lexer(is, stringTable);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());

        // TODO should the next token after EOF be EOF or null or not specified?
        token = lexer.getNextToken();
        Assert.assertNull(token);
    }

    @Test
    public void testComment() throws Exception {
        String comment = " /* aadk ble \n \n\n da t\t\r\n  aaag d  */";
        BufferedInputStream is = getBufferedInputStream(comment);
        Lexer lexer = new Lexer(is, stringTable);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());

        // TODO should the next token after EOF be EOF or null or not specified?
        token = lexer.getNextToken();
        Assert.assertNull(token);
    }

    @Test
    public void testNotTerminatedComment() throws Exception {
        String comment = " /* aadk ble \n \n\n da t\t\r\n  aaag d  ";
        BufferedInputStream is = getBufferedInputStream(comment);
        Lexer lexer = new Lexer(is, stringTable);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.ERROR, token.getType());
    }

    @Test
    public void testMultipleComments() throws Exception {
        String comment = " /* aadk ble  /* \n \n\n da /* t\t\r\n  aaag d  */ ";
        BufferedInputStream is = getBufferedInputStream(comment);
        Lexer lexer = new Lexer(is, stringTable);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());
    }

    @Test
    public void testMultipleCommentTerminations() throws Exception {
        String comment = " /* aadk ble  /* \n \n\n da /* t\t\r\n  aaag d  */ abc */ ";
        BufferedInputStream is = getBufferedInputStream(comment);
        Lexer lexer = new Lexer(is, stringTable);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.IDENTIFIER, token.getType());
        token = lexer.getNextToken();
        Assert.assertEquals(TokenType.ERROR, token.getType());
    }

    private BufferedInputStream getBufferedInputStream(String empty) {
        return new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.US_ASCII)));
    }

}
