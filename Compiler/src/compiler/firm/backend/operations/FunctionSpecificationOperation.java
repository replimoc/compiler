package compiler.firm.backend.operations;

public class FunctionSpecificationOperation extends AssemblerOperation {

	private String name;

	public FunctionSpecificationOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("\t.globl %s\n\t.type\t%s, @function\n", name, name);
	}

}
