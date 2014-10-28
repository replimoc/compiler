package compiler.main;

import java.io.IOException;

import org.junit.Test;

public class CompilerAppTest {

	@Test
	public void testHelp() throws IOException {
		CompilerApp.main(new String[] { "--help" });
	}

	// TODO uncomment, when lexer is not crashing any more
	// @Test
	// public void testLextest() throws IOException {
	// CompilerApp.main(new String[] { "--lextest", "./testdata/EmptyMain.java" });
	// }
}
