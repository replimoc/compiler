package compiler.ast;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassMember extends AstNode {
	private final Symbol identifier;

	public ClassMember(Position position, Symbol identifier) {
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
