package compiler.ast.statement;

import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NullExpression extends PrimaryExpression {

	public NullExpression(Position position) {
		super(position);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
