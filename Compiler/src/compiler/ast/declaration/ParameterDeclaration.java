package compiler.ast.declaration;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ParameterDeclaration extends Declaration {
	private static final String MEMBER_TYPE = "l";
	private int variableNumber;

	public ParameterDeclaration(Position position, Type type, Symbol identifier) {
		super(position, type, identifier);
	}

	public ParameterDeclaration(Type type, Symbol identifier) {
		this(null, type, identifier);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	@Override
	public String getMemberType() {
		return MEMBER_TYPE;
	}
}
