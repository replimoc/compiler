package compiler.ast;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class StaticMethodDeclaration extends MethodDeclaration {

	public StaticMethodDeclaration(Position position, Symbol identifier, Type returnType) {
		super(position, identifier, returnType);
	}

	public StaticMethodDeclaration(Position position, Symbol identifier, Type returnType, Block block) {
		super(position, identifier, returnType, block);
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<ParameterDeclaration> getValidParameters() {
		return new ArrayList<>();
	}

	@Override
	public String getAssemblerName() {
		return "_main"; // Map to main method, StaticMethodDeclaration is always main for MiniJava.
	}
}
