package compiler.parser;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import compiler.lexer.TokenType;
import compiler.utils.TestUtils;

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
		parser = TestUtils.initParser(createTestString("new int[42][42][42];"));
		parser.parse();
	}

	@Test
	public void testExpressionStatement() throws IOException, ParserException {
		Parser parser;

		parser = TestUtils.initParser(createTestString("while(42)ident;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("while(42) return;"));
		parser.parse();

		try {
			parser = TestUtils.initParser(createTestString("while(42)return"));
			parser.parse();
		} catch (ParserException e) {
			Assert.assertEquals(TokenType.RCURLYBRACKET, e.getUnexpectedToken().getType());
		}

		try {
			parser = TestUtils.initParser(createTestString("while(42)ident"));
			parser.parse();
		} catch (ParserException e) {
			Assert.assertEquals(TokenType.RCURLYBRACKET, e.getUnexpectedToken().getType());
		}
	}

	@Test
	public void testWrongPrimaryExpression() throws Exception {
		Parser parser;

		try {
			parser = TestUtils.initParser(createTestString("void;"));
			parser.parse();
			Assert.fail();
		} catch (ParserException e) {
			Assert.assertEquals(TokenType.SEMICOLON, e.getUnexpectedToken().getType());
		}

		try {
			parser = TestUtils.initParser(createTestString("new integer(42);"));
			parser.parse();
			Assert.fail();
		} catch (ParserException e) {
			Assert.assertEquals(TokenType.INTEGER, e.getUnexpectedToken().getType());
		}

		try {
			parser = TestUtils.initParser(createTestString("015;"));
			parser.parse();
			Assert.fail();
		} catch (ParserException e) {
			Assert.assertEquals(TokenType.INTEGER, e.getUnexpectedToken().getType());
			Assert.assertEquals("15", e.getUnexpectedToken().getSymbol().getValue());
		}
	}

	@Test
	public void testPostfixExpression() throws Exception {
		Parser parser;

		// method invocation

		parser = TestUtils.initParser(createTestString("null.callme();"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("identifier.callme(18);"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("false.callme(true, true, true, false);"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("true.callmenot(callme(18));"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("(15.callme().callme().callme().callme().callme(callme()));"));
		parser.parse();

		// field access

		parser = TestUtils.initParser(createTestString("this.field;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("(this).field.field.field.method().field;"));
		parser.parse();

		// array access

		parser = TestUtils.initParser(createTestString("new basic_type[42][][][15][14];"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("val[14][15][16][17];"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("this.callme.callme()[1][2].callme.callme(this);"));
		parser.parse();
	}

	@Test
	public void testUnaryExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("!null;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("-null;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("!new integer();"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("!new array[15][];"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("-new integer();"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("-false;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("!val[14][15][16][17];"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("-this.callme.callme()[1][2].callme.callme(this);"));
		parser.parse();
	}

	@Test
	public void testMulExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("5 * 5 * 6 * 10 * 122 * 2034;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("1244444/123/1231/12/336;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("5%5%5%12;"));
		parser.parse();

		// combinations

		parser = TestUtils.initParser(createTestString("!null * -false;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("!null.callme() * -null.callme * true * false * true[null][false][true[12]].callme();"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("!null % false % (false) / this / this.x;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("new integer() % - new integer().getVlaue();"));
		parser.parse();
	}

	@Test
	public void testAdditiveExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("a + b;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("a - b;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("a + b * c % d - a - b - !c / 125;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("f.x + f.x() * f[x];"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("new int[15] * new integer();"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("a + b - -c;"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("true * false + null % null - (a + b + c -d - -e);"));
		parser.parse();

		parser = TestUtils.initParser(createTestString("this[x] + this.y[x];"));
		parser.parse();
	}

	@Test
	public void testRelationalExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("a < b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a <= b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a > b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a >= b;"));
		parser.parse();

		// chaing

		parser = TestUtils.initParser(createTestString("a < b <= c > d >= e;"));
		parser.parse();

		// some rubbish

		parser = TestUtils.initParser(createTestString("a + b < c + d;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a * b >= c / d;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("this < that;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("this.a >= this[this.a];"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a + b < c < this.x < that.y >= new integer() < new int[123] + new int[123] - x >= -x;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("!a < -b * -c - d;"));
		parser.parse();
	}

	@Test
	public void testEqualityExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("a == b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a != b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("-a != !b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("!a[!a == -b] + b[!b != !a];"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("this != that;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("f(x) != this.f(x)[123] == a + b + new f() != this[f(x)];"));
		parser.parse();
	}

	@Test
	public void testAssignmentExpression() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestString("a = b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("int a = new b();"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("int[]a = new int[b][][][x];"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("false[true] = null = null = false[false][new void[x]];"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a = b && c;"));
		parser.parse();
		parser = TestUtils.initParser(createTestString("a = (b = c) = ((((( d = e= f) != 1 ) = d == 5) = (a || b) = (a - b) = -c ));"));
		parser.parse();
	}

	private static String createTestString(String test) {

		String program = preamble + test + end;
		System.out.println(program);
		return program;
	}
}
