package compiler.ast.statement;

import compiler.ast.Block;

public class IfStatement extends Statement {
	private final Expression condition;
	private final Block trueCase;
	private final Block falseCase;

	public IfStatement(Expression condition, Block trueCase, Block falseCase) {
		this.condition = condition;
		this.trueCase = trueCase;
		this.falseCase = falseCase;
	}

	public IfStatement(Expression condition, Block trueCase) {
		this(condition, trueCase, null);
	}

	public Expression getCondition() {
		return condition;
	}

	public Block getTrueCase() {
		return trueCase;
	}

	public Block getFalseCase() {
		return falseCase;
	}
}
