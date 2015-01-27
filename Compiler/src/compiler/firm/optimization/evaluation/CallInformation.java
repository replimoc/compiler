package compiler.firm.optimization.evaluation;


public class CallInformation {
	private final int constantArguments;

	public CallInformation(int constantArguments) {
		this.constantArguments = constantArguments;
	}

	public int getConstantArguments() {
		return constantArguments;
	}
}
