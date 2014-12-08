package compiler.ast;

import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

import firm.nodes.Node;

public abstract class AstNode {

	protected final Position position;
	protected Type type;
	protected firm.nodes.Node firmNode;

	public AstNode(Position position) {
		this.position = position;
	}

	public AstNode(Position position, Type type) {
		this.position = position;
		this.type = type;
	}

	public Position getPosition() {
		return position;
	}

	public abstract void accept(AstVisitor visitor);

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Node getFirmNode() {
		return firmNode;
	}

	public void setFirmNode(Node firmNode) {
		this.firmNode = firmNode;
	}
}
