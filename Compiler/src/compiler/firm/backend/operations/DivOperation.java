package compiler.firm.backend.operations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class DivOperation extends AssemblerOperation {

	private final RegisterBased dividend;
	private final RegisterBased divisor;
	private final RegisterBased result;
	private final RegisterBased remainder;

	public DivOperation(RegisterBased dividend, RegisterBased divisor, RegisterBased result, RegisterBased remainder) {
		this.dividend = dividend;
		this.divisor = divisor;
		this.result = result;
		this.remainder = remainder;
	}

	@Override
	public String getOperationString() {
		return String.format("\tidiv %s", divisor);
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> commandList = new LinkedList<String>();
		commandList.add(new PushOperation(SingleRegister.RAX).toString());
		commandList.add(new PushOperation(SingleRegister.RDX).toString());

		VirtualRegister raxRegister = new VirtualRegister(SingleRegister.RAX);
		VirtualRegister rdxRegister = new VirtualRegister(SingleRegister.RDX);

		commandList.add(new MovOperation(dividend, raxRegister).toString());
		commandList.add("\tcltd");

		commandList.add(getOperationString());

		if (result != null) {
			commandList.add(new MovOperation(raxRegister, result).toString());
		}
		if (remainder != null) {
			commandList.add(new MovOperation(rdxRegister, remainder).toString());
		}

		commandList.add(new PopOperation(SingleRegister.RDX).toString());
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
}
