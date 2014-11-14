package compiler.ast.statement;

import compiler.lexer.Position;

public abstract class BlockBasedStatement extends Statement {

	public BlockBasedStatement(Position position) {
		super(position);
	}
}
