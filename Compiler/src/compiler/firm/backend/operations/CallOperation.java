package compiler.firm.backend.operations;

public class CallOperation extends AssemblerOperation {

	private String name;

	public CallOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("\tcall %s\n", name);
	}

}
