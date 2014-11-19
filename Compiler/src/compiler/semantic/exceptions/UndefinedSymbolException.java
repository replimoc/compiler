package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class UndefinedSymbolException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492294296L;

	private final Symbol identifier;

	public UndefinedSymbolException(Symbol identifier, Position position) {
		super(position, buildMessage(identifier, position));
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public static String buildMessage(Symbol identifier, Position position) {
		return "error: Identifier " + identifier + " at position " + position + " has not been defined";
	}
}
