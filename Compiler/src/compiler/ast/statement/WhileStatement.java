package compiler.ast.statement;

import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class WhileStatement extends BlockBasedStatement {
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

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}
