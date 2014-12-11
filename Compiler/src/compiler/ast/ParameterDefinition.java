package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ParameterDefinition extends Declaration {
	private static final String MEMBER_TYPE = "f";
	private int variableNumber;

	public ParameterDefinition(Position position, Type type, Symbol identifier) {
		super(position, type, identifier);

	}

	public ParameterDefinition(Type type, Symbol identifier) {
		this(null, type, identifier);

	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public Declaration getDefinition() { // TODO maybe optimize this
		return new LocalVariableDeclaration(getPosition(), getType(), getIdentifier(), variableNumber);
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	@Override
	public boolean isLocalVariable() {
		return false;
	}

	@Override
	public String getMemberType() {
		return MEMBER_TYPE;
	}
}
