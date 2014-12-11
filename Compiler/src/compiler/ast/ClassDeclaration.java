package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.type.ClassType;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassDeclaration extends Declaration implements Comparable<ClassDeclaration> {
	private static final String MEMBER_TYPE = "c";
	private final List<ClassMember> members = new ArrayList<ClassMember>();

	public ClassDeclaration(Position position, Symbol identifier) {
		super(position, identifier);
		setType(new ClassType(identifier));
		setClassDeclaration(this);
	}

	public ClassDeclaration(Symbol identifier, ClassMember... members) {
		this(null, identifier);
		for (ClassMember member : members) {
			addClassMember(member);
			member.setClassDeclaration(this);
		}
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
		return MEMBER_TYPE;
	}
}
