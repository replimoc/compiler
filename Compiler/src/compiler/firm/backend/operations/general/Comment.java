package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;


public class Comment extends AssemblerOperation {

	private String comment;

	public Comment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "\t# " + comment + "\n";
	}
}
