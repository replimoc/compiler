package compiler.ast.type;

import compiler.lexer.Position;

public class MethodType extends Type {

	private final Type returnType;
	private final Type[] parameterTypes;

	public MethodType(Position position, Type returnType, Type[] parameterTypes) {
		super(position, BasicType.METHOD);
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public Type getType() {
		return returnType;
	}

	public Type[] getParameterTypes() {
		return parameterTypes;
	}
}
