package compiler.ast.statement;

import compiler.ast.AstNode;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public abstract class Expression extends AstNode implements Statement {

	public Expression(Position position) {
		super(position);
	}

	public abstract void accept(AstVisitor visitor);
}
