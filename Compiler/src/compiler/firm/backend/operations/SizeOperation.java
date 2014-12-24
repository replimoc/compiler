package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class SizeOperation extends AssemblerOperation {

	private String name;

	public SizeOperation(String name) {
		this.name = name;
	}

	@Override
	public String getOperationString() {
		return String.format("\t.size\t%s, .-%s", name, name);
	}
}
