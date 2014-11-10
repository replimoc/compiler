package compiler.ast.statement.type;

public class ArrayType extends Type {

	private final Type subType;

	public ArrayType(Type subType) {
		super(BasicType.ARRAY);
		this.subType = subType;
	}

	public Type getSubType() {
		return subType;
	}

}
