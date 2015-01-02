package compiler.ast.declaration;

import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MainMethodDeclaration extends MethodDeclaration {

	public MainMethodDeclaration(Position position, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType, Block block) {
		super(position, true, identifier, parameters, returnType, block);
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
		return "_main";
	}
}
