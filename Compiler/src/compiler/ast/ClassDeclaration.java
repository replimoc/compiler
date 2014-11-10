package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;

public class ClassDeclaration {
	private final Symbol identifier;
	private final List<ClassMember> members = new ArrayList<ClassMember>();

	public ClassDeclaration(Symbol identifier) {
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
