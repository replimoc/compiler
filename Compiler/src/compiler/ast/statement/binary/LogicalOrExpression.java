package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class LogicalOrExpression extends BinaryExpression {

	public LogicalOrExpression(Position position, Expression operand1, Expression operand2) {
		super(position, operand1, operand2);
	}

}
