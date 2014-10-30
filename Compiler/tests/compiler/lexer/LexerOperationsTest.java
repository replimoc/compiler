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
 * test correct parsing of operation (arithmetic and logical)
 * <p/>
 * binary ops: != , *= , * , = , += , + , -= , -- , - , <<= , << , <= , < , == , = , >= , >>= ,
 * >>>= , >>> , >> , > , %= , % , &= , && , & ,  ^= , ^ , { , } , ~ , |= , || , |
 * <p/>
 * unary ops:  ! , ++, ~
 * <p/>
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
    public void testAssign() throws Exception {
        String[] expressions = {"a = b", "a= b", "a =b", "a=b"};
        testBinaryOp(TokenType.ASSIGN, expressions);
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
        Assert.assertEquals(TokenType.ADD, lexer.getNextToken().getType());
        Assert.assertEquals(TokenType.ADD, lexer.getNextToken().getType());
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
        Assert.assertNotEquals(TokenType.DECREMENT, lexer.getNextToken().getType());
    }

    @Test
    public void testDivAssign() throws Exception {
        String[] expressions = {"a /= b", "a/= b", "a /=b", "a/=b"};
        testBinaryOp(TokenType.DIVIDEASSIGN, expressions);
    }

    @Test
    public void testDiv() throws Exception {
        String[] expressions = {"a / b", "a/ b", "a /b", "a/b"};
        testBinaryOp(TokenType.DIVIDE, expressions);
    }

    @Test
    public void testLeftShiftAssign() throws Exception {
        String[] expressions = {"a <<= b", "a<<= b", "a <<=b", "a<<=b"};
        testBinaryOp(TokenType.LSASSIGN, expressions);
    }

    @Test
    public void testLeftShift() throws Exception {
        String[] expressions = {"a << b", "a<< b", "a <<b", "a<<b"};
        testBinaryOp(TokenType.LS, expressions);
    }

    @Test
    public void testLessEqual() throws Exception {
        String[] expressions = {"a <= b", "a<= b", "a <=b", "a<=b"};
        testBinaryOp(TokenType.LESSEQUAL, expressions);
    }

    @Test
    public void testLess() throws Exception {
        String[] expressions = {"a < b", "a< b", "a <b", "a<b"};
        testBinaryOp(TokenType.LESS, expressions);
    }

    @Test
    public void testEqual() throws Exception {
        String[] expressions = {"a == b", "a== b", "a ==b", "a==b"};
        testBinaryOp(TokenType.EQUAL, expressions);
    }

    @Test
    public void testGreaterEqual() throws Exception {
        String[] expressions = {"a >= b", "a>= b", "a >=b", "a>=b"};
        testBinaryOp(TokenType.GREATEREQUAL, expressions);
    }

    @Test
    public void testRightShiftAssign() throws Exception {
        String[] expressions = {"a >>= b", "a>>= b", "a >>=b", "a>>=b"};
        testBinaryOp(TokenType.RSASSIGN, expressions);
    }

    @Test
    public void testRightShiftZeroFillAssign() throws Exception {
        String[] expressions = {"a >>>= b", "a>>>= b", "a >>>=b", "a>>>=b"};
        testBinaryOp(TokenType.RSZEROFILLASSIGN, expressions);
    }

    @Test
    public void testRightShiftZeroFill() throws Exception {
        String[] expressions = {"a >>> b", "a>>> b", "a >>>b", "a>>>b"};
        testBinaryOp(TokenType.RSZEROFILL, expressions);
    }

    @Test
    public void testRightShift() throws Exception {
        String[] expressions = {"a >> b", "a>> b", "a >>b", "a>>b"};
        testBinaryOp(TokenType.RS, expressions);
    }

    @Test
    public void testGreater() throws Exception {
        String[] expressions = {"a > b", "a> b", "a >b", "a>b"};
        testBinaryOp(TokenType.GREATER, expressions);
    }
    @Test
    public void testModAssign() throws Exception {
        String[] expressions = {"a %= b", "a%= b", "a %=b", "a%=b"};
        testBinaryOp(TokenType.MODULOASSIGN, expressions);
    }
    @Test
    public void testMod() throws Exception {
        String[] expressions = {"a % b", "a% b", "a %b", "a%b"};
        testBinaryOp(TokenType.MODULO, expressions);
    }
    @Test
    public void testAndAssign() throws Exception {
        String[] expressions = {"a &= b", "a&= b", "a &=b", "a&=b"};
        testBinaryOp(TokenType.ANDASSIGN, expressions);
    }
    @Test
    public void testLogicalAnd() throws Exception {
        String[] expressions = {"a && b", "a&& b", "a &&b", "a&&b"};
        testBinaryOp(TokenType.LOGICALAND, expressions);
    }
    @Test
    public void testBinaryAnd() throws Exception {
        String[] expressions = {"a & b", "a& b", "a &b", "a&b"};
        testBinaryOp(TokenType.AND, expressions);
    }
    @Test
    public void testXorAssign() throws Exception {
        String[] expressions = {"a ^= b", "a^= b", "a ^=b", "a^=b"};
        testBinaryOp(TokenType.EXCLUSIVEORASSIGN, expressions);
    }

    @Test
    public void testXor() throws Exception {
        String[] expressions = {"a ^ b", "a^ b", "a ^b", "a^b"};
        testBinaryOp(TokenType.EXCLUSIVEOR, expressions);
    }

    @Test
    public void testBinaryNot() throws Exception {
        String[] expressions = {"~a", "~ a"};
        testUnaryOp(TokenType.BINARYCOMPLEMENT, expressions);
    }

    @Test
    public void testInclOrAssign() throws Exception {
        String[] expressions = {"a |= b", "a|= b", "a |=b", "a|=b"};
        testBinaryOp(TokenType.INCLUSIVEORASSIGN, expressions);
    }
    @Test
    public void testOr() throws Exception {
        String[] expressions = {"a || b", "a|| b", "a ||b", "a||b"};
        testBinaryOp(TokenType.LOGICALOR, expressions);
    }
    @Test
    public void testBinaryOr() throws Exception {
        String[] expressions = {"a | b", "a| b", "a |b", "a|b"};
        testBinaryOp(TokenType.INCLUSIVEOR, expressions);
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
            Assert.assertEquals("a", a.getValue().getValue());

            Assert.assertEquals(operation, op.getType());

            Assert.assertEquals(TokenType.IDENTIFIER, b.getType());
            Assert.assertEquals("b", b.getValue().getValue());

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
            Assert.assertEquals("a", a.getValue().getValue());

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
            Assert.assertEquals("a", a.getValue().getValue());

            Assert.assertEquals(operation, op.getType());

            Assert.assertEquals(TokenType.EOF, eof.getType());
        }
    }


    private BufferedInputStream getBufferedInputStream(String empty) {
        return new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.US_ASCII)));
    }
}
