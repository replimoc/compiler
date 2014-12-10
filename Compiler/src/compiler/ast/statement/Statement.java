package compiler.ast.statement;

import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

import firm.nodes.Node;

public interface Statement {

	Position getPosition();

	void accept(AstVisitor firmGenerationVisitor);

	Node getFirmNode();
}
