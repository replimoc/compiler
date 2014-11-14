package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MethodDeclaration extends ClassMember {

	private final Type returnType;
	private final List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
	private Block block;

	public MethodDeclaration(Position position, Symbol identifier, Type returnType) {
		super(position, identifier);
		this.returnType = returnType;
	}

	public MethodDeclaration(Position position, Symbol identifier, Type returnType, Block block) {
		super(position, identifier);
		this.returnType = returnType;
	}

	public void addParameter(ParameterDefinition parameter) {
		parameters.add(parameter);
	}

	public Type getType() {
		return returnType;
	}

	public List<ParameterDefinition> getParameters() {
		return parameters;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
