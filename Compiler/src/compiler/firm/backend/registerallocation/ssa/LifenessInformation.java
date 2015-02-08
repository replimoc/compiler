package compiler.firm.backend.registerallocation.ssa;

public class LifenessInformation {
	private final int nextUse;
	private final int lastUse;

	public LifenessInformation(int use) {
		this.lastUse = use;
		this.nextUse = use;
	}

	public LifenessInformation(int nextUse, int lastUse) {
		this.nextUse = nextUse;
		this.lastUse = lastUse;
	}

	public LifenessInformation merge(LifenessInformation other) {
		return new LifenessInformation(Math.min(nextUse, other.nextUse), Math.max(lastUse, other.lastUse));
	}
}
