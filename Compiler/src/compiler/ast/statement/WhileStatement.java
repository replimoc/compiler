package compiler.ast.statement;

import compiler.ast.Block;

public class WhileStatement extends Statement {
	private final Expression condition;
	private final Block body;

	public WhileStatement(Expression condition, Block body) {
		this.condition = condition;
		this.body = body;
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getBody() {
		return body;
	}

}
