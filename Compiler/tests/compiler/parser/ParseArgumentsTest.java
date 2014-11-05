package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseArgumentsTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	// Arguments -> (Expression (, Expression)*)?

	@Test
	public void testEpsilon() throws IOException {
		Parser parser = TestUtils.initParser(")");
		caller.call("parseArguments", parser);
	}

	@Test
	public void testPrimaryExpression() throws IOException {
		Parser parser = TestUtils.initParser("null)");
		caller.call("parseArguments", parser);
	}

	@Test
	public void test2Arguments() throws IOException {
		Parser parser = TestUtils.initParser("false,true)");
		caller.call("parseArguments", parser);
	}

	@Test
	public void testMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser("main.test;");
		caller.call("parseArguments", parser);
	}

	@Test
	public void test3Arguments() throws IOException {
		Parser parser = TestUtils.initParser("false,true,null)");
		caller.call("parseArguments", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFormat() throws IOException {
		Parser parser = TestUtils.initParser("false,,true");
		caller.call("parseArguments", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFormat2() throws IOException {
		Parser parser = TestUtils.initParser("false,,");
		caller.call("parseArguments", parser);
	}

}
