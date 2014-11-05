package compiler.parser;

import compiler.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO document me
 */
public class ParserExpressionsTest {

    static String preamble = "class Test {\n\tpublic void test() {\n\t\t";
    static String end = "\n\t}\n}";

    @Test
    public void testClassDeclarations() throws IOException, ParserException {

    }

    @Test
    public void testPrimaryExpression() throws Exception {
        Parser parser;

        parser = TestUtils.initParser(createTestString("null;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("false;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("true;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("42;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("ident;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("ident();"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("this;"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("(null);"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("new ident();"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("new int[42];"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("new int[42][][][][][][][][];"));
        parser.parse();
        parser = TestUtils.initParser(createTestString("new ident[42];"));
        parser.parse();
    }

    @Test
    public void testWrongPrimaryExpression() throws Exception {
        Parser parser;

        try {
            parser = TestUtils.initParser(createTestString("void;"));
            parser.parse();
            Assert.fail();
        } catch (ParserException e) {
            e.printStackTrace(System.out);
        }

        try {
            parser = TestUtils.initParser(createTestString("new int[42][42][42];"));
            parser.parse();
            Assert.fail();
        } catch (ParserException e) {
            e.printStackTrace(System.out);
        }

    }



    private static String createTestString(String test){

        String program = preamble + test + end;
        System.out.println(program);
        return program;
    }
}
