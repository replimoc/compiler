package compiler.ast.statement;

import compiler.ast.Block;
import compiler.lexer.Position;

public class IfStatement extends Statement {
	private final Expression condition;
	private final Block trueCase;
	private final Block falseCase;

	public IfStatement(Position position, Expression condition, Block trueCase, Block falseCase) {
		super(position);
		this.condition = condition;
		this.trueCase = trueCase;
		this.falseCase = falseCase;
	}

	public IfStatement(Position position, Expression condition, Block trueCase) {
		this(position, condition, trueCase, null);
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
