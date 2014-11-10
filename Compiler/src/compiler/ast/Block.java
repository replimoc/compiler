package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.ast.statement.Statement;

public class Block {
	private final List<Statement> statements = new ArrayList<Statement>();

	public void addStatement(Statement statement) {
		getStatements().add(statement);
	}

	public List<Statement> getStatements() {
		return statements;
	}
}
