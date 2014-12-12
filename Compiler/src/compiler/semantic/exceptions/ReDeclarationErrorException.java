package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class ReDeclarationErrorException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol identifier;

	public ReDeclarationErrorException(Symbol identifier, Position reDeclaration) {
		super(reDeclaration, buildMessage(identifier, reDeclaration));
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Position getReDeclaration() {
		return super.getPosition();
	}

	public static String buildMessage(Symbol identifier, Position reDeclaration) {
		return "error: Identifier " + identifier + " at position " + reDeclaration + " has already been defined.";
	}

}
