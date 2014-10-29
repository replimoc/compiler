package compiler.lexer;

import compiler.StringTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test for correct lexing of integer literals
 */
public class LexerIntegersTest {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testDigits() throws Exception {
        String[] literals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        testIntegerLiterals(literals);
    }

    @Test
    public void testIntegers() throws Exception {
        String[] literals = {"10", "17", "22", "3082034", "4222224", "5642572", "645", "72222", "84", "95711360398520"};
        testIntegerLiterals(literals);
    }

    @Test
    public void testNoNulls() throws Exception {
        String[] literals = {"01234"};
        // TODO pending advisor's answer
//        testIntegerLiterals(literals);
    }

    @Test
    public void testNegativeNumbers() throws Exception {
        String[] literals = {"-0", "-13", "-200", "-3639293", "-478787", "-533335"};
        testNegativeIntegerLiterals(literals);
    }

    private void testIntegerLiterals(String[] literals) throws IOException {
        for (String literal : literals) {

            BufferedInputStream is = getBufferedInputStream(literal);
            Lexer lexer = new Lexer(is, stringTable);
            Token literalToken = lexer.getNextToken();
            Token eof = lexer.getNextToken();

            Assert.assertEquals(TokenType.INTEGER, literalToken.getType());
            Assert.assertEquals(literal, literalToken.getValue());

            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }

    private void testNegativeIntegerLiterals(String[] literals) throws IOException {
        for (String literal : literals) {

            BufferedInputStream is = getBufferedInputStream(literal);
            Lexer lexer = new Lexer(is, stringTable);
            Token minusToken = lexer.getNextToken();
            Token literalToken = lexer.getNextToken();
            Token eof = lexer.getNextToken();

            Assert.assertEquals(TokenType.SUBTRACT, minusToken.getType());
            Assert.assertEquals(TokenType.INTEGER, literalToken.getType());
            // TODO check javadoc for substring
            Assert.assertEquals(literal.substring(1, literal.length()), literalToken.getValue());
            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }

    private BufferedInputStream getBufferedInputStream(String empty) {
        return new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.US_ASCII)));
    }
}
