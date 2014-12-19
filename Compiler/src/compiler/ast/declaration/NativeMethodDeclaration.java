package compiler.ast.declaration;

import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NativeMethodDeclaration extends MethodMemberDeclaration {

	public NativeMethodDeclaration(Position position, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType) {
		super(position, identifier, parameters, returnType);
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "";
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
