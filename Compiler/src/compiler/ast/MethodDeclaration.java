package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.statement.type.Type;

public class MethodDeclaration extends ClassMember {

	private final Type returnType;
	private final List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
	private final Block block = new Block();

	public MethodDeclaration(Type returnType, Symbol identifier) {
		super(identifier);
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
}
