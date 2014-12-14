package compiler.ast.statement;

import compiler.ast.AstNode;
import compiler.lexer.Position;

public abstract class BlockBasedStatement extends AstNode implements Statement {

	public BlockBasedStatement(Position position) {
		super(position);
	}
}
