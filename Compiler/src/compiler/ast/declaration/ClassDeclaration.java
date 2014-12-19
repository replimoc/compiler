package compiler.ast.declaration;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.type.ClassType;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ClassDeclaration extends Declaration implements Comparable<ClassDeclaration> {
	private final List<MemberDeclaration> members = new ArrayList<MemberDeclaration>();

	public ClassDeclaration(Position position, Symbol identifier) {
		super(position, identifier);
		setType(new ClassType(identifier));
		setClassDeclaration(this);
	}

	public ClassDeclaration(Symbol identifier, MemberDeclaration... members) {
		this(null, identifier);
		for (MemberDeclaration member : members) {
			addClassMember(member);
			member.setClassDeclaration(this);
		}
	}

	public void addClassMember(MemberDeclaration member) {
		this.members.add(member);
	}

	public List<MemberDeclaration> getMembers() {
		return members;
	}

	@Override
	public ClassType getType() {
		return (ClassType) super.getType();
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "c$";
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int compareTo(ClassDeclaration o) {
		return getIdentifier().getValue().compareTo(o.getIdentifier().getValue());
	}
}
