package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class ModuloExpression extends BinaryExpression {

	public ModuloExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
