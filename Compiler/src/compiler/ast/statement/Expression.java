package compiler.ast.statement;

import compiler.lexer.Position;

public abstract class Expression extends Statement {

	public Expression(Position position) {
		super(position);
	}

}
