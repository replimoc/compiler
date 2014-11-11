package compiler.ast.statement;

import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public abstract class Expression extends Statement {

	public Expression(Position position) {
		super(position);
	}

	public abstract void accept(AstVisitor visitor);
}
