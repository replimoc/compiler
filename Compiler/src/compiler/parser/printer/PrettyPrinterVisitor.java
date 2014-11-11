package compiler.parser.printer;

import compiler.ast.AstVisitor;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.ArrayAccessExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.BinaryExpression;
import compiler.ast.statement.binary.DivisionExpression;
import compiler.ast.statement.binary.EqualityExpression;
import compiler.ast.statement.binary.GreaterThanEqualExpression;
import compiler.ast.statement.binary.GreaterThanExpression;
import compiler.ast.statement.binary.LessThanEqualExpression;
import compiler.ast.statement.binary.LessThanExpression;
import compiler.ast.statement.binary.LogicalAndExpression;
import compiler.ast.statement.binary.LogicalOrExpression;
import compiler.ast.statement.binary.ModuloExpression;
import compiler.ast.statement.binary.MuliplicationExpression;
import compiler.ast.statement.binary.NonEqualityExpression;
import compiler.ast.statement.binary.SubtractionExpression;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.lexer.TokenType;

public class PrettyPrinterVisitor implements AstVisitor {
	private String outputString = "";

	/**
	 * Gets the result of the PrettyPrinterVisitor
	 * 
	 * @return
	 */
	public String getOutputString() {
		return this.outputString;
	}

	/**
	 * Visit a binary expression, use the TokenType to display the symbol and show the brackets.
	 * 
	 * @param binaryExpression
	 *            Expression to display
	 * @param tokenType
	 *            Type of the token.
	 */
	private void visit(BinaryExpression binaryExpression, TokenType tokenType) {
		// TODO Show only brackets if it is necessary.
		outputString += "(";
		binaryExpression.getOperand1().accept(this);
		outputString += tokenType.getString();
		binaryExpression.getOperand2().accept(this);
		outputString += ")";
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		visit(additionExpression, TokenType.ADD);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		visit(assignmentExpression, TokenType.ASSIGN);
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		visit(divisionExpression, TokenType.DIVIDE);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		visit(equalityExpression, TokenType.EQUAL);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		visit(greaterThanEqualExpression, TokenType.GREATEREQUAL);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		visit(greaterThanExpression, TokenType.GREATER);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		visit(lessThanEqualExpression, TokenType.LESSEQUAL);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		visit(lessThanExpression, TokenType.LESS);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		visit(logicalAndExpression, TokenType.LOGICALAND);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		visit(logicalOrExpression, TokenType.LOGICALOR);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		visit(moduloExpression, TokenType.MODULO);
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		visit(multiplicationExpression, TokenType.MULTIPLY);
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		visit(nonEqualityExpression, TokenType.NOTEQUAL);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		visit(substractionExpression, TokenType.SUBTRACT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// outputString += variableAccessExpression.getIdentifier().getValue();
		outputString += "_"; // FIXME This should be the real identifier
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NegateExpression negateExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		// TODO Auto-generated method stub

	}

}
