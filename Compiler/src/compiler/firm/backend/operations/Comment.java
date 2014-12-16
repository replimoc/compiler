package compiler.firm.backend.operations;

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
