package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class GreaterThanExpression extends BinaryExpression {

	public GreaterThanExpression(Position position, Expression operand1, Expression operand2) {
		super(position, operand1, operand2);
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
