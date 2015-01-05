package compiler.ast.declaration;

import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.firm.backend.calling.CallingConvention;
import compiler.lexer.Position;

public class NativeMethodDeclaration extends MethodMemberDeclaration {

	public NativeMethodDeclaration(Position position, boolean isStatic, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType) {
		super(position, isStatic, identifier, parameters, returnType);
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "";
	}

	@Override
	public CallingConvention getCallingConvention() {
		return CallingConvention.SYSTEMV_ABI;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isNative() {
		return true;
	}
}
