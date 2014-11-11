package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Literal extends Expression {
	private final Symbol symbol;

	public Literal(Position position, Symbol symbol) {
		super(position);
		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}