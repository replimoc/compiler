package compiler.ast;

import java.util.LinkedList;
import java.util.List;

import compiler.ast.statement.Statement;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Block extends Statement {

	private final List<Statement> statements = new LinkedList<Statement>();

	public Block(Position position) {
		super(position);
	}

	public Block(Statement statement) {
		super(statement.getPosition());
		statements.add(statement);
	}

	public void addStatement(Statement statement) {
		getStatements().add(statement);
	}

	public List<Statement> getStatements() {
		return statements;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public int getNumberOfStatements() {
		return statements.size();
	}

	public boolean isEmpty() {
		return statements.isEmpty();
	}
}
