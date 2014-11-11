package compiler.ast.statement;

import compiler.ast.Block;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class WhileStatement extends Statement {
	private final Expression condition;
	private final Block body;

	public WhileStatement(Position position, Expression condition, Block body) {
		super(position);
		this.condition = condition;
		this.body = body;
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getBody() {
		return body;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}
