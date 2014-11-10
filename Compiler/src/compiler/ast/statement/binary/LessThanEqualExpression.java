package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class LessThanEqualExpression extends BinaryExpression {

	public LessThanEqualExpression(Position position, Expression operand1, Expression operand2) {
		super(position, operand1, operand2);
	}

}
