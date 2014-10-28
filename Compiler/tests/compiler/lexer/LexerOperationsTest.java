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
 * test correct parsing of operation
 * <p/>
 * binary ops: != , *= , * , = , += , + , -= , -- , - ,  TODO from here . , , , : , ; , <<= , << , <= , < , == , = , >= , >>= ,
 * >>>= , >>> , >> , > , ? , %= , % , &= , && , & , [ , ] , ^= , ^ , { , } , ~ , |= , || , |
 * <p/>
 * unary ops:  ! , ++,
 * <p/>
 * TODO ops: ( , ) , ',' ,
 */
public class LexerOperationsTest {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testNotEqual() throws Exception {
        String[] expressions = {"a != b", "a!= b", "a !=b", "a!=b"};
        testBinaryOp(TokenType.NOTEQUAL, expressions);
    }

    @Test
    public void testNot() throws Exception {
        String[] expressions = {"!a", "! a"};
        testUnaryOp(TokenType.LOGICALNOT, expressions);
    }

    @Test
    public void testEqual() throws Exception {
        String[] expressions = {"a = b", "a= b", "a =b", "a=b"};
        testBinaryOp(TokenType.EQUAL, expressions);
    }

    @Test
    public void testMulAssign() throws Exception {
        String[] expressions = {"a *= b", "a*= b", "a *=b", "a*=b"};
        testBinaryOp(TokenType.MULTIPLYASSIGN, expressions);
    }

    @Test
    public void testMul() throws Exception {
        String[] expressions = {"a * b", "a* b", "a *b", "a*b"};
        testBinaryOp(TokenType.MULTIPLY, expressions);
    }

    @Test
    public void testPreIncrement() throws Exception {
        String[] expressions = {"++a", "++ a"};
        testUnaryOp(TokenType.INCREMENT, expressions);

        // test that "+ + with a break between them are not lexed as increment
        String expression = "+ + a";
        BufferedInputStream is = getBufferedInputStream(expression);
        Lexer lexer = new Lexer(is, stringTable);
        Token first = lexer.getNextToken();
        Assert.assertNotEquals(TokenType.INCREMENT, first.getType());
    }

    @Test
    public void testPostIncrement() throws Exception {
        String[] expressions = {"a++", "a ++"};
        testPostfixOp(TokenType.INCREMENT, expressions);

        // test that "+ + with a break between them are not lexed as increment
        String expression = "a + +";
        BufferedInputStream is = getBufferedInputStream(expression);
        Lexer lexer = new Lexer(is, stringTable);
        lexer.getNextToken(); // skip a
        Token second = lexer.getNextToken();
        Assert.assertEquals(TokenType.INCREMENT, second.getType());
    }

    @Test
    public void testAddAssign() throws Exception {
        String[] expressions = {"a += b", "a+= b", "a +=b", "a+=b"};
        testBinaryOp(TokenType.ADDASSIGN, expressions);
    }

    @Test
    public void testAdd() throws Exception {
        String[] expressions = {"a + b", "a+ b", "a +b", "a+b"};
        testBinaryOp(TokenType.ADD, expressions);
    }

    @Test
    public void testSubAssign() throws Exception {
        String[] expressions = {"a -= b", "a-= b", "a -=b", "a-=b"};
        testBinaryOp(TokenType.SUBTRACTASSIGN, expressions);
    }

    @Test
    public void testSub() throws Exception {
        String[] expressions = {"a - b", "a- b", "a -b", "a-b"};
        testBinaryOp(TokenType.SUBTRACT, expressions);
    }

    @Test
    public void testPreDecrement() throws Exception {
        String[] expressions = {"--a", "-- a"};
        testUnaryOp(TokenType.DECREMENT, expressions);

        // test that "+ + with a break between them are not lexed as increment
        String expression = "- - a";
        BufferedInputStream is = getBufferedInputStream(expression);
        Lexer lexer = new Lexer(is, stringTable);
        Token first = lexer.getNextToken();
        Assert.assertNotEquals(TokenType.DECREMENT, first.getType());
    }

    @Test
    public void testPostDecrement() throws Exception {
        String[] expressions = {"a--", "a --"};
        testPostfixOp(TokenType.DECREMENT, expressions);

        // test that "+ + with a break between them are not lexed as increment
        String expression = "a - -";
        BufferedInputStream is = getBufferedInputStream(expression);
        Lexer lexer = new Lexer(is, stringTable);
        lexer.getNextToken(); // skip a
        Token second = lexer.getNextToken();
        Assert.assertEquals(TokenType.DECREMENT, second.getType());
    }

    private void testBinaryOp(TokenType operation, String[] expressions) throws IOException {
        for (String expression : expressions) {

            BufferedInputStream is = getBufferedInputStream(expression);
            Lexer lexer = new Lexer(is, stringTable);
            Token a = lexer.getNextToken();
            Token op = lexer.getNextToken();
            Token b = lexer.getNextToken();
            Token eof = lexer.getNextToken();

            Assert.assertEquals(TokenType.IDENTIFIER, a.getType());
//            Assert.assertEquals('a', a.getValue()); // TODO method to get string rep of a symbol

            Assert.assertEquals(operation, op.getType());

            Assert.assertEquals(TokenType.IDENTIFIER, b.getType());
//            Assert.assertEquals('b', b.getValue()); // TODO method to get string rep of a symbol

            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }

    private void testUnaryOp(TokenType operation, String[] expressions) throws IOException {
        for (String expression : expressions) {

            BufferedInputStream is = getBufferedInputStream(expression);
            Lexer lexer = new Lexer(is, stringTable);
            Token op = lexer.getNextToken();
            Token a = lexer.getNextToken();
            Token eof = lexer.getNextToken();

            Assert.assertEquals(operation, op.getType());

            Assert.assertEquals(TokenType.IDENTIFIER, a.getType());
//            Assert.assertEquals('a', a.getValue()); // TODO method to get string rep of a symbol

            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }

    private void testPostfixOp(TokenType operation, String[] expressions) throws IOException {
        for (String expression : expressions) {

            BufferedInputStream is = getBufferedInputStream(expression);
            Lexer lexer = new Lexer(is, stringTable);
            Token a = lexer.getNextToken();
            Token op = lexer.getNextToken();
            Token eof = lexer.getNextToken();

            Assert.assertEquals(TokenType.IDENTIFIER, a.getType());
//            Assert.assertEquals('a', a.getValue()); // TODO method to get string rep of a symbol

            Assert.assertEquals(operation, op.getType());

            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }


    private BufferedInputStream getBufferedInputStream(String empty) {
        return new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.US_ASCII)));
    }
}
