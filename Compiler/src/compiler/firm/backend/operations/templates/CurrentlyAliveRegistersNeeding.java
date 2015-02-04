package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.storage.RegisterBundle;

public interface CurrentlyAliveRegistersNeeding {
	public void setAliveRegisters(Set<RegisterBundle> registers);
}
