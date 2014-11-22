package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ParameterDefinition extends AstNode {

	private final Symbol identifier;

	public ParameterDefinition(Position position, Type type, Symbol identifier) {
		super(position);
		setType(type);
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
