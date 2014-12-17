package compiler.firm.backend.operations;

public class Debug extends AssemblerOperation {

	private final String operation;

	public Debug(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return String.format("\t%s\n", operation);
	}

}
