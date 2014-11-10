package compiler.ast.statement.unary;

import compiler.ast.AstVisitor;
import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class NegateExpression extends UnaryExpression {

	public NegateExpression(Position position, Expression operand) {
		super(position, operand);
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
