package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseArrayAccessTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	// ArrayAccess -> [ Expression ]

	@Test
	public void testPrimaryExpr() throws IOException {
		Parser parser = TestUtils.initParser("[null]");
		caller.call("parseArrayAccess", parser);
	}

	@Test
	public void testIdent() throws IOException {
		Parser parser = TestUtils.initParser("[main]");
		caller.call("parseArrayAccess", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testWrongBegin() throws IOException {
		Parser parser = TestUtils.initParser("(main.test]");
		caller.call("parseArrayAccess", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testWrongExpr() throws IOException {
		Parser parser = TestUtils.initParser("[main,test]");
		caller.call("parseArrayAccess", parser);
	}
}
