package compiler.firm.backend.operations;

import compiler.utils.Utils;

public class FunctionSpecificationOperation extends AssemblerOperation {

	private String name;

	public FunctionSpecificationOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		if (Utils.isWindows()) {
			return String.format("\t.globl %s\n\t.def\t%s;  .scl    2;      .type   32;     .endef\n", name, name);
		} else {
			return String.format("\t.globl %s\n\t.type\t%s, @function\n", name, name);
		}
	}
}
