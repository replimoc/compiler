package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.AstVisitor;
import compiler.lexer.Position;

public class NewObjectExpression extends Expression {

	private final Symbol identifier;
	private final Expression[] parameters;

	public NewObjectExpression(Position position, Symbol identifier, Expression[] parameters) {
		super(position);
		this.identifier = identifier;
		this.parameters = parameters;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Expression[] getParameters() {
		return parameters;
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
