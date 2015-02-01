package compiler.firm.backend.storage;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.TestRuntimeException;
import compiler.firm.backend.Bit;
import compiler.firm.backend.registerallocation.Interval;

public class VirtualRegister extends RegisterBased {

	private static int I = 0;

	private final Bit mode;
	private final int num;
	private final LinkedList<Interval> lifetimes = new LinkedList<>();
	private final List<VirtualRegister> preferedRegisters = new LinkedList<>();
	private final LinkedHashSet<VirtualRegister> interferences;
	private final String comment;

	private Storage register;
	private boolean isSpilled;

	public VirtualRegister(Bit mode) {
		this(mode, (String) null);
	}

	public VirtualRegister(Bit mode, String comment) {
		this(mode, (RegisterBased) null, comment);
	}

	public VirtualRegister(SingleRegister register) {
		this(register.getMode(), register, null);
	}

	public VirtualRegister(Bit mode, RegisterBased register, String comment) {
		this.mode = mode;
		this.register = register;
		this.num = I++;
		this.interferences = new LinkedHashSet<>();
		this.comment = comment;
	}

	public VirtualRegister(Bit mode, RegisterBundle registerBundle) {
		this(mode, registerBundle.getRegister(mode), null);
	}

	@Override
	public String toString() {
		return register == null ? "VR_" + getNum() : register.toString();
		// return "VR_" + getNum();
	}

	public Storage getRegister() {
		return register;
	}

	public void setStorage(Storage register) {
		this.register = register;
	}

	public void setSpilled(boolean isSpilled) {
		this.isSpilled = isSpilled;
	}

	@Override
	public boolean isSpilled() {
		return isSpilled;
	}

	private Interval getLifetimeInterval(int line) {
		for (Interval curr : lifetimes) {
			if (curr.contains(line)) {
				return curr;
			}
		}
		return null;
	}

	@Override
	public Bit getMode() {
		return mode;
	}

	public boolean isAliveAt(int line) {
		return getLifetimeInterval(line) != null;
	}

	public int getNum() {
		return num;
	}

	@Override
	public SingleRegister getSingleRegister() {
		return register == null ? null : register.getSingleRegister();
	}

	@Override
	public RegisterBundle getRegisterBundle() {
		return register == null ? null : register.getRegisterBundle();
	}

	public void addPreferedRegister(VirtualRegister preferedRegister) {
		this.preferedRegisters.add(preferedRegister);
		preferedRegister.preferedRegisters.add(this);
	}

	public Set<RegisterBundle> getPreferedRegisterBundles() {
		Set<RegisterBundle> preferredBundles = new HashSet<RegisterBundle>();

		for (VirtualRegister preferred : this.preferedRegisters) {
			if (preferred.getRegister() != null) {
				preferredBundles.add(preferred.getRegisterBundle());
			}
		}

		return preferredBundles;
	}

	@Override
	public MemoryPointer getMemoryPointer() {
		return register.getMemoryPointer();
	}

	@Override
	public void setTemporaryStackOffset(int temporaryStackOffset) {
		register.setTemporaryStackOffset(temporaryStackOffset);
	}

	public void expandLifetime(int line, boolean read) {
		if (lifetimes.isEmpty()) {
			if (read) {
				throw new TestRuntimeException("first use of register is not a write: VR_" + this.num);
			}

			lifetimes.addLast(new Interval(line));
			return;
		} else if (!read) {
			throw new TestRuntimeException("second definition of variable: VR_" + this.num);
		}

		Interval lastInterval = lifetimes.getLast();
		lastInterval.expandEnd(line);
	}

	public String getLifetimes() {
		StringBuilder builder = new StringBuilder();
		for (Interval curr : lifetimes) {
			builder.append(' ');
			builder.append(curr);
		}
		return builder.toString();
	}

	public LinkedList<Interval> getLiftimeIntervals() {
		return lifetimes;
	}

	public void addInteference(Set<VirtualRegister> interferences) {
		for (VirtualRegister register : interferences) {
			if (!equals(register)) {
				this.interferences.add(register);
			}
		}
	}

	public LinkedHashSet<VirtualRegister> getInterferences() {
		return this.interferences;
	}

	@Override
	public String getComment() {
		return comment;
	}

}
