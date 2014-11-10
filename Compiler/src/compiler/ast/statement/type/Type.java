package compiler.ast.statement.type;

public class Type {

	private final BasicType basicType;

	public Type(BasicType basicType) {
		this.basicType = basicType;
	}

	public BasicType getBasicType() {
		return basicType;
	}
}
