package compiler.semantic.exceptions;

import compiler.ast.AstNode;

public class TypeErrorException extends SemanticAnalysisException {
	private static final long serialVersionUID = -271485782975658532L;

	public TypeErrorException(AstNode astNode) {
		this(astNode, null);
	}

	public TypeErrorException(AstNode astNode, String message) {
		super(astNode.getPosition(), buildMessage(astNode, message));
	}

	public static String buildMessage(AstNode astNode, String message) {
		String classString = astNode.getClass() != null ? astNode.getClass().toString() : "unknown";
		String positionString = astNode.getPosition() != null ? astNode.getPosition().toString() : "unknown";
		String messageString = message != null ? ": " + message : "";
		return "error: type error in token " + classString + " at position " + positionString + messageString;
	}
}
