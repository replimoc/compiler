package compiler.firm.backend.storage;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.Bit;

import firm.nodes.Const;

public class Constant extends Storage {
	private final int constant;

	public Constant(int constant) {
		this.constant = constant;
	}

	public Constant(Const constNode) {
		this.constant = constNode.getTarval().asInt();
	}

	@Override
	public String toString() {
		String result;
		if (constant < 0) {
			result = String.format("$-0x%x", -constant);
		} else {
			result = String.format("$0x%x", constant);
		}
		return result;
	}

	@Override
	public Set<RegisterBased> getReadRegistersOnRightSide() {
		return Collections.emptySet();
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Collections.emptySet();
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

	@Override
	public boolean isSpilled() {
		return false;
	}

	public int getConstant() {
		return constant;
	}

	@Override
	public Bit getMode() {
		return null;
	}

	@Override
	public SingleRegister getSingleRegister() {
		return null;
	}

	@Override
	public RegisterBundle getRegisterBundle() {
		return null;
	}

	@Override
	public MemoryPointer getMemoryPointer() {
		return null;
	}

	@Override
	public void setTemporaryStackOffset(int temporaryStackOffset) {
	}
}
