package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class NoSuchMemberException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol object;
	private final Symbol member;
	private final Position objDefinition;
	private final Position memberDefinition;

	public NoSuchMemberException(Symbol object, Position objDefinition, Symbol member, Position memberDefinition) {
		super(memberDefinition, buildMessage(object, objDefinition, member, memberDefinition));
		this.object = object;
		this.objDefinition = objDefinition;
		this.member = member;
		this.memberDefinition = memberDefinition;
	}

	public Symbol getObjectIdentifier() {
		return object;
	}

	public Symbol getMemberIdentifier() {
		return member;
	}

	public Position getObjectDefinition() {
		return objDefinition;
	}

	public Position getMemberDefinition() {
		return memberDefinition;
	}

	public static String buildMessage(Symbol object, Position objDefinition, Symbol member, Position memberDefinition) {
		return "error: Member " + member + " at position " + memberDefinition + " is not available in object " + object + " defined at "
				+ objDefinition;
	}
}
