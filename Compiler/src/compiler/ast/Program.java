package compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class Program {
	private final List<ClassDeclaration> classes = new ArrayList<ClassDeclaration>();

	public void addClassDeclaration(ClassDeclaration classDeclaration) {
		this.classes.add(classDeclaration);
	}

	public List<ClassDeclaration> getClasses() {
		return classes;
	}
}
