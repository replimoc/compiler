package compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import compiler.ast.statement.NewArrayExpression;
import compiler.lexer.Position;
import compiler.lexer.TokenType;
import compiler.parser.printer.PrettyPrinter;
import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseNewExpressionTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	private static Class<?>[] parameterTypes;
	private static Object[] args;

	@BeforeClass
	public static void setUp() {
		parameterTypes = new Class<?>[1];
		parameterTypes[0] = Position.class;
		args = new Position[1];
		args[0] = new Position(0, 0);
	}

	@Test
	public void testWithIdent() throws IOException {
		Parser parser = TestUtils.initParser("test()");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test
	public void testWithArray() throws IOException {
		Parser parser = TestUtils.initParser("test [42];");
		assertEquals("new test[42]", callNewArrayExpression(parser));
	}

	@Test
	public void testWithArray2dim() throws IOException {
		Parser parser = TestUtils.initParser("test [true][];");
		assertEquals("new test[true][]", callNewArrayExpression(parser));
	}

	@Test
	public void testWithArrayAssignment() throws IOException {
		Parser parser = TestUtils.initParser("test [null=null][][][];");
		assertEquals("new test[null = null][][][]", callNewArrayExpression(parser));
	}

	@Test
	public void testWithInt() throws IOException {
		Parser parser = TestUtils.initParser("int [this][];");
		assertEquals("new int[this][]", callNewArrayExpression(parser));
	}

	@Test
	public void testWithBool() throws IOException {
		Parser parser = TestUtils.initParser("boolean [fancyexpression=42][];");
		assertEquals("new boolean[fancyexpression = 42][]", callNewArrayExpression(parser));
	}

	@Test
	public void testWithVoid() throws IOException {
		Parser parser = TestUtils.initParser("void [main][];");
		assertEquals("new void[main][]", callNewArrayExpression(parser));
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidExpressionInArray() throws IOException {
		Parser parser = TestUtils.initParser("test [(]");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidNewObject() throws IOException {
		Parser parser = TestUtils.initParser("test )[]");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test
	public void testExpressionInArray3dim() throws IOException {
		Parser parser = TestUtils.initParser("test [false][][true]");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidObject() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.BOOLEAN, TokenType.LP, TokenType.RP);
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidArray() throws IOException {
		Parser parser = TestUtils.initParser( // test ([])
				TokenType.IDENTIFIER, TokenType.LP, TokenType.LSQUAREBRACKET, TokenType.RSQUAREBRACKET, TokenType.RP);
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidTwoDimArray() throws IOException {
		Parser parser = TestUtils.initParser( // test [[]]
				TokenType.IDENTIFIER, TokenType.LSQUAREBRACKET, TokenType.LSQUAREBRACKET, TokenType.RSQUAREBRACKET, TokenType.RSQUAREBRACKET);
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidExprEnd() throws IOException {
		Parser parser = TestUtils.initParser("int[42)");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidNewExprBegin() throws IOException {
		Parser parser = TestUtils.initParser("[42)");
		caller.call("parseNewExpression", parser, parameterTypes, args);
	}

	private String callNewArrayExpression(Parser parser) {
		return PrettyPrinter.get((NewArrayExpression) caller.call("parseNewExpression", parser, parameterTypes, args));
	}

}
