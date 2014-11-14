package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
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
}
