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
        Lexer lexer = initLexer(empty);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());

        // TODO should the next token after EOF be EOF or null or not specified?
        token = lexer.getNextToken();
        Assert.assertNull(token);
    }

    @Test
    public void testComment() throws Exception {
        String comment = " /* aadk ble \n \n\n da t\t\r\n  aaag d  */";
        Lexer lexer = initLexer(comment);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());

        // TODO should the next token after EOF be EOF or null or not specified?
        token = lexer.getNextToken();
        Assert.assertNull(token);
    }

    @Test
    public void testNotTerminatedComment() throws Exception {
        String comment = " /* aadk ble \n \n\n da t\t\r\n  aaag d  ";
        Lexer lexer = initLexer(comment);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.ERROR, token.getType());
    }

    @Test
    public void testMultipleComments() throws Exception {
        String comment = " /* aadk ble  /* \n \n\n da /* t\t\r\n  aaag d  */ ";
        Lexer lexer = initLexer(comment);

        Token token = lexer.getNextToken();
        Assert.assertEquals(TokenType.EOF, token.getType());
    }

    @Test
    public void testMultipleCommentTerminations() throws Exception {
        String comment = " /* aadk ble  /* \n \n\n da /* t\t\r\n  aaag d  */ abc */ ";
        Lexer lexer = initLexer(comment);

        Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
        Assert.assertEquals(TokenType.MULTIPLY, lexer.getNextToken().getType());
        Assert.assertEquals(TokenType.DIVIDE, lexer.getNextToken().getType());
        Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
    }

    private Lexer initLexer(String program) throws IOException {
        BufferedInputStream is =
                new BufferedInputStream(new ByteArrayInputStream(program.getBytes(StandardCharsets.US_ASCII)));
        return new Lexer(is, stringTable);
    }

}
