package compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MethodDeclaration extends ClassMember {
	private static final String MEMBER_TYPE = "m";

	private final Type returnType;
	private final List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
	private Block block;
	private int numberOfLocalVariables;

	public MethodDeclaration(Position position, Symbol identifier, Type returnType) {
		super(position, identifier);
		this.returnType = returnType;
	}

	public MethodDeclaration(Symbol identifier, Type returnType, ParameterDefinition... parameters) {
		this(null, identifier, returnType);
		if (parameters != null && parameters.length > 0) {
			this.parameters.addAll(Arrays.asList(parameters));
		}
	}

	public MethodDeclaration(Position position, Symbol identifier, Type returnType, Block block) {
		super(position, identifier);
		this.returnType = returnType;
		this.block = block;
	}

	public void addParameter(ParameterDefinition parameter) {
		parameters.add(parameter);
	}

	@Override
	public Type getType() {
		return returnType;
	}

	public List<ParameterDefinition> getParameters() {
		return parameters;
	}

	public List<ParameterDefinition> getValidParameters() {
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

	@Override
	protected int getSortPriority() {
		return 0;
	}

	public void setNumberOfRequiredLocals(int numberOfLocals) {
		this.setNumberOfLocalVariables(numberOfLocals);
	}

	public int getNumberOfLocalVariables() {
		return numberOfLocalVariables;
	}

	public void setNumberOfLocalVariables(int numberOfLocalVariables) {
		this.numberOfLocalVariables = numberOfLocalVariables;
	}

	@Override
	public String getMemberType() {
		return MEMBER_TYPE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodDeclaration other = (MethodDeclaration) obj;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

}
