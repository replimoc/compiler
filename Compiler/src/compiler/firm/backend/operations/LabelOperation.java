package compiler.firm.backend.operations;

public class LabelOperation extends AssemblerOperation {

	private String name;

	public LabelOperation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s:\n", name);
	}

}
