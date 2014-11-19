package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class RedefinitionErrorException extends Exception {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol identifier;
	private final Position definition;
	private final Position redefinition;

	public RedefinitionErrorException(Symbol identifier, Position definition, Position redefinition) {
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

	@Override
	public String toString() {
		return "error: Identifier " + identifier + " at position " + redefinition + " has already been definied at " + definition;
	}

	@Override
	public String getMessage() {
		return toString();
	}
}
