package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassType extends Type {

	private final Symbol identifier;

	public ClassType(Position position, Symbol identifier) {
		super(position, BasicType.CLASS);
		this.identifier = identifier;
	}

	@Override
	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
