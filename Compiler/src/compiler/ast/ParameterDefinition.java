package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.symbolTable.LocalVariableDefinition;

public class ParameterDefinition extends AstNode {

	private final Symbol identifier;
	private int variableNumber;

	public ParameterDefinition(Position position, Type type, Symbol identifier) {
		super(position, type);
		this.identifier = identifier;

	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public LocalVariableDefinition getDefinition() {
		return new LocalVariableDefinition(identifier, getType(), variableNumber);
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}
}
