package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class NoSuchMemberException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol object;
	private final Symbol member;
	private final Position objectDeclaration;

	public NoSuchMemberException(Symbol object, Position objectDeclaration, Symbol member, Position memberDeclaration) {
		super(memberDeclaration, buildMessage(object, objectDeclaration, member, memberDeclaration));
		this.object = object;
		this.objectDeclaration = objectDeclaration;
		this.member = member;
	}

	public Symbol getObjectIdentifier() {
		return object;
	}

	public Symbol getMemberIdentifier() {
		return member;
	}

	public Position getObjectDeclaration() {
		return objectDeclaration;
	}

	public Position getMemberDeclaration() {
		return super.getPosition();
	}

	public static String buildMessage(Symbol object, Position objectDeclaration, Symbol member, Position memberDeclaration) {
		return "error: Member " + member + " at position " + memberDeclaration + " is not available in object " + object + " defined at "
				+ objectDeclaration;
	}
}
