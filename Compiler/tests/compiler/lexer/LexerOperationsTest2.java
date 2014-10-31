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
 * Test other operators - brackets, commas, etc
 * <p/>
 * ops: ( , ), , ,  . , : , ? , ;, [ , ], { , }
 *
 * @author effenok
 */
public class LexerOperationsTest2 {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testEmptyMethodCall() throws Exception {
        String[] methodCalls = {"void main()", "void main \n ( \n )", "void main (/* something wrong here*/)"};
        for (String expression : methodCalls) {
            Lexer lexer = initLexer(expression);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LP, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RP, lexer.getNextToken().getType());
        }
    }

    @Test
    public void testMethodCallOneArg() throws Exception {
        String[] methodCalls = {"void main(int x)", "void main \n ( int\tx )", "void main (/*unsigned*/int x)"};
        for (String expression : methodCalls) {
            Lexer lexer = initLexer(expression);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LP, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RP, lexer.getNextToken().getType());
        }
    }

    @Test
    public void testMethodCallTwoArgs() throws Exception {
        String[] methodCalls = {"void main(int x, int y)", "void main \n ( int\tx \n \n , \t\t\t int y )",
                "void main (/*unsigned*/int x, int /* unknown */  y)"};
        for (String expression : methodCalls) {
            Lexer lexer = initLexer(expression);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LP, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.COMMA, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RP, lexer.getNextToken().getType());
        }
    }

    @Test
    public void testClassMembers() throws Exception {
        {
            String field = "clazzie.Variable";
            Lexer lexer = initLexer(field);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.POINT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
        }

        {
            String method = "clazzie.Method()";
            Lexer lexer = initLexer(method);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.POINT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LP, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RP, lexer.getNextToken().getType());
        }

        {
            String method = "clazzie.Method(arrrrrg)";
            Lexer lexer = initLexer(method);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.POINT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LP, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RP, lexer.getNextToken().getType());
        }
    }

    @Test
    public void testEOLs() throws Exception {
        {
            String lines = ";;\n\n\n\t;\n";
            Lexer lexer = initLexer(lines);
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
        }

        {
            String line = "a = b + c;";
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.ASSIGN, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.ADD, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
        }

        {
            String lines = "a %= b; c;\n\nint \n d \t; public\n";
            Lexer lexer = initLexer(lines);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.MODULOASSIGN, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());

            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());

            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());

            Assert.assertEquals(TokenType.PUBLIC, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
        }

    }

    /**
     * TODO what is the proper name for  x = expr ? true_val : false_val?
     */
    @Test
    public void testQuestionColon() throws Exception {
        {
            String[] expressions = {"a = iff ? /* */ then : /****/ ellse ", "v=a?b:c"};
            for (String expression : expressions) {
                Lexer lexer = initLexer(expression);
                Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.ASSIGN, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.COLON, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            }

            {
                String expr = "?:::??\n????";
                Lexer lexer = initLexer(expr);
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.COLON, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.COLON, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.COLON, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
                Assert.assertEquals(TokenType.CONDITIONAL, lexer.getNextToken().getType());
            }
        }
    }

    @Test
    public void testMethodBodies() throws Exception {
        String[] lines = {"void method {;;}", "void method {\n\t;\t;\n}"};
        for (String line : lines) {
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
        }
        {
            String line = "void method { {} {} {}";
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
        }
        {
            String line = "void method {a++;b--;c;return;}";
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.VOID, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INCREMENT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.DECREMENT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RETURN, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
        }
    }

    @Test
    public void testArrays() throws Exception {
        {
            String line = "String[] args = {5}";
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LSQUAREBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RSQUAREBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.ASSIGN, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INTEGER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RCURLYBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.EOF, lexer.getNextToken().getType());
        }
        {
            String line = "int[] vars = new int[15]";
            Lexer lexer = initLexer(line);
            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LSQUAREBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RSQUAREBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.IDENTIFIER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.ASSIGN, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.NEW, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INT, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.LSQUAREBRACKET, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.INTEGER, lexer.getNextToken().getType());
            Assert.assertEquals(TokenType.RSQUAREBRACKET, lexer.getNextToken().getType());
        }

    }

    private Lexer initLexer(String program) throws IOException {
        BufferedInputStream is =
                new BufferedInputStream(new ByteArrayInputStream(program.getBytes(StandardCharsets.US_ASCII)));
        return new Lexer(is, stringTable);
    }

}
