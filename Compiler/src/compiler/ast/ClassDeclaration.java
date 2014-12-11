package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassDeclaration extends Declaration implements Comparable<ClassDeclaration> {
	private final List<ClassMember> members = new ArrayList<ClassMember>();

	public ClassDeclaration(Position position, Symbol identifier) {
		super(position, identifier);
	}

	public void addClassMember(ClassMember member) {
		this.members.add(member);
	}

	public List<ClassMember> getMembers() {
		return members;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int compareTo(ClassDeclaration o) {
		return getIdentifier().getValue().compareTo(o.getIdentifier().getValue());
	}

	@Override
	public String getMemberType() {
		return null;
	}
}
