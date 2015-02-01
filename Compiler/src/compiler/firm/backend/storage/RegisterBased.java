package compiler.firm.backend.storage;

import java.util.Collections;
import java.util.Set;

import compiler.utils.Utils;

public abstract class RegisterBased extends Storage {
	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(this);
	}

	@Override
	public Set<RegisterBased> getReadRegistersOnRightSide() {
		return Collections.emptySet();
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(this);
	}

	@Override
	public boolean isSpilled() {
		return false;
	}

	public String getComment() {
		return "";
	}
}
