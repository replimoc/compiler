package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.lexer.Position;

public class ClassDeclaration extends AstNode {
	private final Symbol identifier;
	private final List<ClassMember> members = new ArrayList<ClassMember>();

	public ClassDeclaration(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public void addClassMember(ClassMember member) {
		this.members.add(member);
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public List<ClassMember> getMembers() {
		return members;
	}
}
