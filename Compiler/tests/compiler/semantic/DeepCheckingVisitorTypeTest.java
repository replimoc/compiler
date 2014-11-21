package compiler.semantic;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.type.BasicType;
import compiler.lexer.Position;
import compiler.semantic.exceptions.TypeErrorException;

public class DeepCheckingVisitorTypeTest {

	private HashMap<Symbol, ClassScope> classScopes = new HashMap<Symbol, ClassScope>();
	private DeepCheckingVisitor visitor = new DeepCheckingVisitor(classScopes);
	private Position position = new Position(0, 0);
	private IntegerConstantExpression integer = new IntegerConstantExpression(position, "42");
	private BooleanConstantExpression btrue = new BooleanConstantExpression(position, true);

	private void visitAndAssertTypeEquals(AstNode node, BasicType type) {
		node.accept(visitor);
		assertEquals(type, node.getType().getBasicType());
		assertEquals(0, visitor.getExceptions().size());
	}

	private void visitAndThrowAllVisitorExceptions(AstNode node) throws Exception {
		node.accept(visitor);
		for (Exception exception : visitor.getExceptions()) {
			throw exception;
		}
	}

	@Test
	public void testVisitAdditionExpressionInt() {
		visitAndAssertTypeEquals(new AdditionExpression(position, integer, integer), BasicType.INT);
	}

	@Test(expected = TypeErrorException.class)
	public void testVisitAdditionExpressionBool() throws Exception {
		visitAndThrowAllVisitorExceptions(new AdditionExpression(position, btrue, btrue));
	}

	@Test(expected = TypeErrorException.class)
	public void testVisitAdditionExpressionMixed() throws Exception {
		visitAndThrowAllVisitorExceptions(new AdditionExpression(position, btrue, integer));
	}

}
