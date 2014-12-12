package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * 
 */
public class ParserExpressionsTest {

	private static String preamble = "class Test {\n\tpublic void test() {\n\t\t";
	private static String end = "\n\t}\n}";

	@Test
	public void testPrimaryExpression() throws Exception {
		TestUtils.parse(createTestString("null;"));

		TestUtils.parse(createTestString("false;"));

		TestUtils.parse(createTestString("true;"));

		TestUtils.parse(createTestString("42;"));

		TestUtils.parse(createTestString("ident;"));

		TestUtils.parse(createTestString("ident();"));

		TestUtils.parse(createTestString("this;"));

		TestUtils.parse(createTestString("(null);"));

		TestUtils.parse(createTestString("new ident();"));

		TestUtils.parse(createTestString("new int[42];"));

		TestUtils.parse(createTestString("new int[42][][][][][][][][];"));

		TestUtils.parse(createTestString("new ident[42];"));

		TestUtils.parse(createTestString("new int[42][42][42];"));

	}

	@Test
	public void testExpressionStatement() throws IOException, ParsingFailedException {
		TestUtils.parse(createTestString("while(42)ident;"));
		TestUtils.parse(createTestString("while(42) return;"));
	}

	@Test(expected = ParsingFailedException.class)
	public void testExpressionStatement2() throws IOException, ParsingFailedException {
		TestUtils.parse(createTestString("while(42)return"));
	}

	@Test(expected = ParsingFailedException.class)
	public void testExpressionStatement3() throws IOException, ParsingFailedException {
		TestUtils.parse(createTestString("while(42)ident"));
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongPrimaryExpression1() throws Exception {
		TestUtils.parse(createTestString("void;"));
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongPrimaryExpression2() throws Exception {
		TestUtils.parse(createTestString("new integer(42);"));
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongPrimaryExpression3() throws Exception {
		TestUtils.parse(createTestString("015;"));
	}

	@Test
	public void testMethodInvocation() throws Exception {
		TestUtils.parse(createTestString("null.callme();"));

		TestUtils.parse(createTestString("identifier.callme(18);"));

		TestUtils.parse(createTestString("false.callme(true, true, true, false);"));

		TestUtils.parse(createTestString("true.callmenot(callme(18));"));

		TestUtils.parse(createTestString("(15.callme().callme().callme().callme().callme(callme()));"));
	}

	@Test
	public void testFieldAccess() throws Exception {
		TestUtils.parse(createTestString("this.field;"));

		TestUtils.parse(createTestString("(this).field.field.field.method().field;"));
	}

	@Test
	public void testArrayAccess() throws Exception {
		TestUtils.parse(createTestString("new basic_type[42][][][15][14];"));

		TestUtils.parse(createTestString("val[14][15][16][17];"));

		TestUtils.parse(createTestString("this.callme.callme()[1][2].callme.callme(this);"));
	}

	@Test
	public void testUnaryExpression() throws Exception {
		TestUtils.parse(createTestString("!null;"));

		TestUtils.parse(createTestString("-null;"));

		TestUtils.parse(createTestString("!new integer();"));

		TestUtils.parse(createTestString("!new array[15][];"));

		TestUtils.parse(createTestString("-new integer();"));

		TestUtils.parse(createTestString("-false;"));

		TestUtils.parse(createTestString("!val[14][15][16][17];"));

		TestUtils.parse(createTestString("-this.callme.callme()[1][2].callme.callme(this);"));
	}

	@Test
	public void testMulExpression() throws Exception {
		TestUtils.parse(createTestString("5 * 5 * 6 * 10 * 122 * 2034;"));

		TestUtils.parse(createTestString("1244444/123/1231/12/336;"));

		TestUtils.parse(createTestString("5%5%5%12;"));

		// combinations

		TestUtils.parse(createTestString("!null * -false;"));

		TestUtils.parse(createTestString("!null.callme() * -null.callme * true * false * true[null][false][true[12]].callme();"));

		TestUtils.parse(createTestString("!null % false % (false) / this / this.x;"));

		TestUtils.parse(createTestString("new integer() % - new integer().getVlaue();"));
	}

	@Test
	public void testAdditiveExpression() throws Exception {
		TestUtils.parse(createTestString("a + b;"));

		TestUtils.parse(createTestString("a - b;"));

		TestUtils.parse(createTestString("a + b * c % d - a - b - !c / 125;"));

		TestUtils.parse(createTestString("f.x + f.x() * f[x];"));

		TestUtils.parse(createTestString("new int[15] * new integer();"));

		TestUtils.parse(createTestString("a + b - -c;"));

		TestUtils.parse(createTestString("true * false + null % null - (a + b + c -d - -e);"));

		TestUtils.parse(createTestString("this[x] + this.y[x];"));
	}

	@Test
	public void testRelationalExpression() throws Exception {
		TestUtils.parse(createTestString("a < b;"));

		TestUtils.parse(createTestString("a <= b;"));

		TestUtils.parse(createTestString("a > b;"));

		TestUtils.parse(createTestString("a >= b;"));

		// chaining
		TestUtils.parse(createTestString("a < b <= c > d >= e;"));

		// some rubbish

		TestUtils.parse(createTestString("a + b < c + d;"));

		TestUtils.parse(createTestString("a * b >= c / d;"));

		TestUtils.parse(createTestString("this < that;"));

		TestUtils.parse(createTestString("this.a >= this[this.a];"));

		TestUtils.parse(createTestString("a + b < c < this.x < that.y >= new integer() < new int[123] + new int[123] - x >= -x;"));

		TestUtils.parse(createTestString("!a < -b * -c - d;"));

	}

	@Test
	public void testEqualityExpression() throws Exception {
		TestUtils.parse(createTestString("a == b;"));

		TestUtils.parse(createTestString("a != b;"));

		TestUtils.parse(createTestString("-a != !b;"));

		TestUtils.parse(createTestString("!a[!a == -b] + b[!b != !a];"));

		TestUtils.parse(createTestString("this != that;"));

		TestUtils.parse(createTestString("f(x) != this.f(x)[123] == a + b + new f() != this[f(x)];"));
	}

	@Test
	public void testAssignmentExpression() throws Exception {
		TestUtils.parse(createTestString("a = b;"));

		TestUtils.parse(createTestString("int a = new b();"));

		TestUtils.parse(createTestString("int[]a = new int[b][][][x];"));

		TestUtils.parse(createTestString("false[true] = null = null = false[false][new void[x]];"));

		TestUtils.parse(createTestString("a = b && c;"));

		TestUtils.parse(createTestString("a = (b = c) = ((((( d = e= f) != 1 ) = d == 5) = (a || b) = (a - b) = -c ));"));
	}

	private static String createTestString(String test) {
		String program = preamble + test + end;
		System.out.println(program);
		return program;
	}
}
