package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.ast.statement.Statement;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Block extends AstNode {

	private final List<Statement> statements = new ArrayList<Statement>();

	public Block(Position position) {
		super(position);
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
}
