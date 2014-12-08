package compiler.ast.statement;

import compiler.ast.AstNode;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public abstract class Statement extends AstNode {

	public Statement(Position position) {
		super(position);
	}

	public Statement(Position position, Type type) {
		super(position, type);
	}

}
