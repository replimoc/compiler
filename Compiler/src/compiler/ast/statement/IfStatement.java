package compiler.ast.statement;

import compiler.lexer.Position;

public class IfStatement extends Statement {
	private final Expression condition;
	private final Statement trueCase;
	private final Statement falseCase;

	public IfStatement(Position position, Expression condition, Statement trueCase, Statement falseCase) {
		super(position);
		this.condition = condition;
		this.trueCase = trueCase;
		this.falseCase = falseCase;
	}

	public IfStatement(Position position, Expression condition, Statement trueCase) {
		this(position, condition, trueCase, null);
	}

	public Expression getCondition() {
		return condition;
	}

	public Statement getTrueCase() {
		return trueCase;
	}

	public Statement getFalseCase() {
		return falseCase;
	}
}
