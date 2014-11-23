package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class IllegalAccessToNonStaticMemberException extends SemanticAnalysisException {
	private static final long serialVersionUID = 9074801993003507209L;

	public IllegalAccessToNonStaticMemberException(Position pos) {
		super(pos, "error: Illegal access to non static member at " + pos);
	}

}
