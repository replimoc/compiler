package compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.Expression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.parser.printer.PrettyPrinter;
import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParsePostfixOpTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;
	private static Class<?>[] parameterTypes;
	private static Object[] args;

	@BeforeClass
	public static void setUp() {
		parameterTypes = new Class<?>[1];
		parameterTypes[0] = Expression.class;
		args = new Expression[1];
		args[0] = new DummyExpression(null);
	}

	// PostfixOp -> MethodInvocationFieldAccess | ArrayAccess

	@Test
	public void testFieldAccess() throws IOException {
		Parser parser = TestUtils.initParser(".main;");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test
	public void testMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser(".main(null)");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test
	public void testMethodInvocationWithVoid() throws IOException {
		Parser parser = TestUtils.initParser(".main()");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test
	public void testArrayAccess() throws IOException {
		Parser parser = TestUtils.initParser("[42]");
		assertEquals("[42]", callArrayAccessExpression(parser));
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFieldAccess() throws IOException {
		Parser parser = TestUtils.initParser(".main(");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidMethodInvocation() throws IOException {
		Parser parser = TestUtils.initParser(".main(null");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidField() throws IOException {
		Parser parser = TestUtils.initParser(".*");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidBegin() throws IOException {
		Parser parser = TestUtils.initParser("*");
		caller.call("parsePostfixOp", parser, parameterTypes, args);
	}

	private static class DummyExpression extends Expression {
		public DummyExpression(Position position) {
			super(position);
		}

		@Override
		public void accept(AstVisitor visitor) {
		}
	}

	private String callArrayAccessExpression(Parser parser) {
		return PrettyPrinter.prettyPrint((ArrayAccessExpression) caller.call("parsePostfixOp", parser, parameterTypes, args)).toString();
	}
}
