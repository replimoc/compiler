package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
import compiler.lexer.Position;

public class MethodDeclaration extends ClassMember {

	private final Type returnType;
	private final List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
	private final Block block;

	public MethodDeclaration(Position position, Type returnType, Symbol identifier, Block block) {
		super(position, identifier);
		this.returnType = returnType;
		this.block = block;
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
}
