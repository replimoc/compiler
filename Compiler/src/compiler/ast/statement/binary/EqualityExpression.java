package compiler.ast.statement.binary;

import compiler.ast.AstVisitor;
import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class EqualityExpression extends BinaryExpression {

	public EqualityExpression(Position position, Expression operand1, Expression operand2) {
		super(position, operand1, operand2);
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
