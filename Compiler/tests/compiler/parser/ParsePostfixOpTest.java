package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParsePostfixOpTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	// PostfixOp -> MethodInvocationFieldAccess | ArrayAccess

	@Test
	public void testFieldAccess() throws IOException {
		Parser parser = TestUtils.initParser(".main;");
		caller.call("parsePostfixOp", parser);
	}

	@Test
	public void testMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser(".main(null)");
		caller.call("parsePostfixOp", parser);
	}

	@Test
	public void testMethodInvocationWithVoid() throws IOException {
		Parser parser = TestUtils.initParser(".main()");
		caller.call("parsePostfixOp", parser);
	}

	@Test
	public void testArrayAccess() throws IOException {
		Parser parser = TestUtils.initParser("[42]");
		caller.call("parsePostfixOp", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFieldAccess() throws IOException {
		Parser parser = TestUtils.initParser(".main(");
		caller.call("parsePostfixOp", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser(".main(null");
		caller.call("parsePostfixOp", parser);
	}

}
