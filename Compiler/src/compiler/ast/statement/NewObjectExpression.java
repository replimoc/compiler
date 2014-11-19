package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NewObjectExpression extends PrimaryExpression {

	private final Symbol identifier;

	public NewObjectExpression(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}
