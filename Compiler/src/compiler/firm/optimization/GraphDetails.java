package compiler.firm.optimization;

public class GraphDetails {
	private final boolean hasSideEffects;

	public GraphDetails(boolean hasSideEffects) {
		this.hasSideEffects = hasSideEffects;
	}

	public boolean hasSideEffects() {
		return hasSideEffects;
	}
}
