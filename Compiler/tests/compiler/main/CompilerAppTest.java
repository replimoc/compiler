package compiler.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.PrivateMethodCaller;

public class CompilerAppTest {
	private final PrivateMethodCaller methodCaller = new PrivateMethodCaller(CompilerApp.class);

	@Test
	public void testHelp() throws IOException {
		assertEquals(0, callExecute("--help"));
	}

	@Test
	public void testNoOption() throws IOException {
		assertEquals(1, callExecute());
	}

	@Test
	public void testUnknownOption() throws IOException {
		assertEquals(1, callExecute("--unknown"));
	}

	@Test
	public void testLextest() throws IOException {
		assertEquals(0, callExecute("--lextest", "./testdata/EmptyMain.java"));
	}

	@Test
	public void testLextestUnknownFile() throws IOException {
		assertEquals(1, callExecute("--lextest", "./testdata/NONEXISTING_FILE.java"));
	}

	@Test
	public void testParseUnknownFile() throws IOException {
		assertEquals(1, callExecute("./testdata/NONEXISTING_FILE.java"));
	}

	@Test
	public void testParseFile() throws IOException {
		assertEquals(0, callExecute("./testdata/outputTest/LinkedListInsertion.java"));
	}

	@Test
	public void testParseInvalidFile() throws IOException {
		assertEquals(1, callExecute("./testdata/parser5/MissingCommaInParameterList.java"));
	}

	private int callExecute(String... args) {
		return methodCaller.call("execute", null, new Object[] { args });
	}
}
