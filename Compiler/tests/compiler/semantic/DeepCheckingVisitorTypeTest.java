package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

	@Test
	public void testVisitAssignmentExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitDivisionExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitEqualityExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitGreaterThanEqualExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitGreaterThanExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLessThanEqualExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLessThanExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLogicalAndExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLogicalOrExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitModuloExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitMuliplicationExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitNonEqualityExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitSubtractionExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitBooleanConstantExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitIntegerConstantExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitMethodInvocationExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitNewArrayExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitNewObjectExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitVariableAccessExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitArrayAccessExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLogicalNotExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitNegateExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitReturnStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitThisExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitNullExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitType() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitBlock() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitClassDeclaration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitIfStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitWhileStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitLocalVariableDeclaration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitParameterDefinition() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitProgram() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitMethodDeclaration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitFieldDeclaration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitStaticMethodDeclaration() {
		fail("Not yet implemented"); // TODO
	}

}
