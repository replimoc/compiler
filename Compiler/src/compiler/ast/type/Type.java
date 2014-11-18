package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Type extends AstNode {

	private final BasicType basicType;

	public Type(Position position, BasicType basicType) {
		super(position);
		this.basicType = basicType;
	}

	public BasicType getBasicType() {
		return basicType;
	}

	public Type getSubType() {
		return null;
	}

	public Symbol getIdentifier() {
		return null;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
