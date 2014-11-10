package compiler.ast.statement;

import compiler.ast.AstNode;
import compiler.lexer.Position;

public abstract class Statement extends AstNode {

	public Statement(Position position) {
		super(position);
	}

}
