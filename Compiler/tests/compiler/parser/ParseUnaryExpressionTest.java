package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseUnaryExpressionTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	// UnaryExpression -> PostfixExpression | (! | -) UnaryExpression

	@Test
	public void testNull() throws IOException {
		Parser parser = TestUtils.initParser("null;");
		caller.call("parseUnaryExpression", parser);
	}

	@Test
	public void testTrue() throws IOException {
		Parser parser = TestUtils.initParser("true;");
		caller.call("parseUnaryExpression", parser);
	}

	@Test
	public void testMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser("main.call();");
		caller.call("parseUnaryExpression", parser);
	}

	@Test
	public void testNegatedMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser("!main.call();");
		caller.call("parseUnaryExpression", parser);
	}

	@Test
	public void testCallMultipleMethods() throws IOException {
		Parser parser = TestUtils.initParser("main.call().call().call(true);");
		caller.call("parseUnaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidCall() throws IOException {
		Parser parser = TestUtils.initParser("-.call().");
		caller.call("parseUnaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidMethod() throws IOException {
		Parser parser = TestUtils.initParser("!true.([42])");
		caller.call("parseUnaryExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidCallMultipleMethods() throws IOException {
		Parser parser = TestUtils.initParser("main.call()[]");
		caller.call("parseUnaryExpression", parser);
	}
}
