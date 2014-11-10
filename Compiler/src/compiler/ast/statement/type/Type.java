package compiler.ast.statement.type;

import compiler.ast.AstNode;
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
}
