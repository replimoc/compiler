package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParsePrimaryExpressionTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	@Test
	public void testNull() throws IOException {
		Parser parser = TestUtils.initParser("null");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testFalse() throws IOException {
		Parser parser = TestUtils.initParser("false");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testTrue() throws IOException {
		Parser parser = TestUtils.initParser("true");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testIdent() throws IOException {
		Parser parser = TestUtils.initParser("test");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testIdent2() throws IOException {
		Parser parser = TestUtils.initParser("(test)");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testIdentWithArguments() throws IOException {
		Parser parser = TestUtils.initParser("test()");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidIdentWithArguments() throws IOException {
		Parser parser = TestUtils.initParser("test(");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testIdentWithMissingBracket() throws IOException {
		Parser parser = TestUtils.initParser("(test");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testInt() throws IOException {
		Parser parser = TestUtils.initParser("42");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testThis() throws IOException {
		Parser parser = TestUtils.initParser("this");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testArray() throws IOException {
		Parser parser = TestUtils.initParser("new int[17][];");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testArrayWithOr() throws IOException {
		Parser parser = TestUtils.initParser("new int[true||false][];");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test
	public void testObject() throws IOException {
		Parser parser = TestUtils.initParser("new test()");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testOperator() throws IOException {
		Parser parser = TestUtils.initParser("+");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidObject() throws IOException {
		Parser parser = TestUtils.initParser("new int()");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidNew() throws IOException {
		Parser parser = TestUtils.initParser("new +");
		caller.call("parsePrimaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidArray() throws IOException {
		Parser parser = TestUtils.initParser("new int[[]]");
		caller.call("parsePrimaryExpression", parser);
	}

}
