package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class UndefinedSymbolException extends Exception {
	private static final long serialVersionUID = -1109058515492294296L;

	private final Symbol identifier;
	private final Position definition;

	public UndefinedSymbolException(Symbol identifier, Position definition) {
		this.identifier = identifier;
		this.definition = definition;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Position getDefinition() {
		return definition;
	}
	
	@Override
	public String toString() {
		return "error: Identifier " + identifier + " at position " + definition + " has not been defined";
	}

	@Override
	public String getMessage() {
		return toString();
	}
}
