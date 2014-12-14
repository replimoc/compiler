package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Program extends AstNode {

	public Program(Position position) {
		super(position);
	}

	private final List<ClassDeclaration> classes = new ArrayList<ClassDeclaration>();

	public void addClassDeclaration(ClassDeclaration classDeclaration) {
		this.classes.add(classDeclaration);
	}

	public List<ClassDeclaration> getClasses() {
		return classes;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
