package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class RedefinitionErrorException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol identifier;
	private final Position definition;
	private final Position redefinition;

	public RedefinitionErrorException(Symbol identifier, Position definition, Position redefinition) {
		super(redefinition, buildMessage(identifier, definition, redefinition));
		this.identifier = identifier;
		this.definition = definition;
		this.redefinition = redefinition;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Position getDefinition() {
		return definition;
	}

	public Position getRedefinition() {
		return redefinition;
	}

	public static String buildMessage(Symbol identifier, Position definition, Position redefinition) {
		return "error: Identifier " + identifier + " at position " + redefinition + " has already been definied at " + definition;
	}

}
