package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NewObjectExpression extends PrimaryExpression {

	private final Symbol identifier;
	private ClassDeclaration objectClass;

	public NewObjectExpression(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public void setObjectClass(ClassDeclaration objectClass) {
		this.objectClass = objectClass;
	}

	public ClassDeclaration getObjectClass() {
		return objectClass;
	}
}
