package compiler.ast.statement;

import compiler.lexer.Position;

public class WhileStatement extends Statement {
	private final Expression condition;
	private final Statement body;

	public WhileStatement(Position position, Expression condition, Statement body) {
		super(position);
		this.condition = condition;
		this.body = body;
	}

	public Expression getCondition() {
		return condition;
	}

	public Statement getBody() {
		return body;
	}

}
