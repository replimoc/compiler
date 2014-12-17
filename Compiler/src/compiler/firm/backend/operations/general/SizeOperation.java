package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;


public class SizeOperation extends AssemblerOperation {

	private String name;

	public SizeOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("\t.size\t%s, .-%s\n", name, name);
	}
}
