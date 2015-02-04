package compiler.firm.backend.operations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.CurrentlyAliveRegistersNeeding;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class DivOperation extends AssemblerOperation implements CurrentlyAliveRegistersNeeding {

	private final RegisterBased dividend;
	private final RegisterBased divisor;
	private final RegisterBased result;
	private final RegisterBased remainder;
	private final Set<RegisterBundle> aliveRegisters = new HashSet<>();

	public DivOperation(RegisterBased dividend, RegisterBased divisor, RegisterBased result, RegisterBased remainder) {
		this.dividend = dividend;
		if (dividend instanceof VirtualRegister) {
			((VirtualRegister) dividend).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.divisor = divisor;

		this.result = result;
		if (result != null && result instanceof VirtualRegister) {
			((VirtualRegister) result).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.remainder = remainder;
		if (remainder != null && remainder instanceof VirtualRegister) {
			((VirtualRegister) remainder).addPreferedRegister(new VirtualRegister(SingleRegister.RDX));
		}
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv %s", divisor);
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (result != null && !result.isSpilled())
			aliveRegisters.remove(result.getRegisterBundle());
		if (remainder != null && !remainder.isSpilled())
			aliveRegisters.remove(remainder.getRegisterBundle());

		List<String> commandList = new LinkedList<String>();

		if (aliveRegisters.contains(RegisterBundle._AX))
			commandList.add(new PushOperation(SingleRegister.RAX).toString());
		if (aliveRegisters.contains(RegisterBundle._DX))
			commandList.add(new PushOperation(SingleRegister.RDX).toString());

		commandList.add(new MovOperation(dividend, SingleRegister.EAX).toString());
		commandList.add("\tcltd");

		commandList.add(getOperationString());

		if (result != null) {
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EAX, result).toStringWithSpillcode()));
		}
		if (remainder != null) {
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EDX, remainder).toStringWithSpillcode()));
		}

		if (aliveRegisters.contains(RegisterBundle._DX))
			commandList.add(new PopOperation(SingleRegister.RDX).toString());
		if (aliveRegisters.contains(RegisterBundle._AX))
			commandList.add(new PopOperation(SingleRegister.RAX).toString());

		return commandList.toArray(new String[0]);
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(dividend, divisor);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		if (dividend == null && remainder == null) {
			return Collections.emptySet();
		} else if (dividend == null) {
			return Utils.unionSet(remainder);
		} else if (remainder == null) {
			return Utils.unionSet(dividend);
		} else {
			return Utils.unionSet(remainder, dividend);
		}
	}

	@Override
	public void setAliveRegisters(Set<RegisterBundle> registers) {
		this.aliveRegisters.clear();
		this.aliveRegisters.addAll(registers);
	}
}
