package compiler.firm.backend.operations.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public abstract class FixedTwoSourceTwoDestinationOperation extends AssemblerOperation implements CurrentlyAliveRegistersNeeding {

	private final Storage source1;
	private final Storage source2;
	private final RegisterBased destination1;
	private final RegisterBased destination2;
	private final Set<RegisterBundle> aliveRegisters = new HashSet<>();

	public FixedTwoSourceTwoDestinationOperation(String comment, Storage source1, Storage source2, RegisterBased destination1,
			RegisterBased destination2) {
		super(comment);

		this.source1 = source1;
		if (source1 instanceof VirtualRegister) {
			((VirtualRegister) source1).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.source2 = source2;

		this.destination1 = destination1;
		if (destination1 != null && destination1 instanceof VirtualRegister) {
			((VirtualRegister) destination1).addPreferedRegister(new VirtualRegister(SingleRegister.RAX));
		}

		this.destination2 = destination2;
		if (destination2 != null && destination2 instanceof VirtualRegister) {
			((VirtualRegister) destination2).addPreferedRegister(new VirtualRegister(SingleRegister.RDX));
		}
	}

	@Override
	public final String getOperationString() {
		return null;
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (destination1 != null && !destination1.isSpilled())
			aliveRegisters.remove(destination1.getRegisterBundle());
		if (destination2 != null && !destination2.isSpilled())
			aliveRegisters.remove(destination2.getRegisterBundle());

		List<String> commandList = new LinkedList<String>();

		int temporaryStackOffset = 0;
		MemoryPointer raxSaved = null;
		boolean source2OnRax = !source2.isSpilled() && source2.getRegisterBundle() == RegisterBundle._AX;
		if (aliveRegisters.contains(RegisterBundle._AX) || source2OnRax) {
			commandList.add(new PushOperation(SingleRegister.RAX).toString());
			temporaryStackOffset += X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE;
			raxSaved = new MemoryPointer(0, SingleRegister.RSP);
		}
		boolean source2OnRdx = !source2.isSpilled() && source2.getRegisterBundle() == RegisterBundle._DX;
		MemoryPointer rdxSaved = null;
		if (aliveRegisters.contains(RegisterBundle._DX) || source2OnRdx) {
			commandList.add(new PushOperation(SingleRegister.RDX).toString());
			temporaryStackOffset += X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE;
			rdxSaved = new MemoryPointer(0, SingleRegister.RSP);

			if (raxSaved != null)
				raxSaved.setTemporaryStackOffset(X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE);
		}

		Storage usedSource2;
		if (source2OnRax) {
			usedSource2 = raxSaved;
		} else if (source2OnRdx) {
			usedSource2 = rdxSaved;
		} else {
			usedSource2 = source2;
		}

		source1.setTemporaryStackOffset(temporaryStackOffset);
		commandList.add(new MovOperation(source1, SingleRegister.EAX).toString());
		source1.setTemporaryStackOffset(0);
		source2.setTemporaryStackOffset(temporaryStackOffset);
		commandList.addAll(getOperationString(source2.getMode(), usedSource2));
		source2.setTemporaryStackOffset(0);

		if ((!aliveRegisters.contains(RegisterBundle._AX) && source2OnRax) || (!aliveRegisters.contains(RegisterBundle._DX) && source2OnRdx)) {
			commandList.add(new AddOperation(new Constant(X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE), SingleRegister.RSP, SingleRegister.RSP).toString());
		}
		if (destination1 != null) {
			destination1.setTemporaryStackOffset(temporaryStackOffset);
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EAX, destination1).toString()));
			destination1.setTemporaryStackOffset(0);
		}
		if (destination2 != null) {
			destination2.setTemporaryStackOffset(temporaryStackOffset);
			commandList.addAll(Arrays.asList(new MovOperation(SingleRegister.EDX, destination2).toString()));
			destination2.setTemporaryStackOffset(0);
		}

		if (aliveRegisters.contains(RegisterBundle._DX))
			commandList.add(new PopOperation(SingleRegister.RDX).toString());
		if (aliveRegisters.contains(RegisterBundle._AX))
			commandList.add(new PopOperation(SingleRegister.RAX).toString());

		return commandList.toArray(new String[0]);
	}

	public abstract List<String> getOperationString(Bit mode, Storage source2);

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(source1.getReadRegisters(), source2.getReadRegisters());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		if (destination1 == null && destination2 == null) {
			return Collections.emptySet();
		} else if (destination1 == null) {
			return Utils.unionSet(destination2);
		} else if (destination2 == null) {
			return Utils.unionSet(destination1);
		} else {
			return Utils.unionSet(destination2, destination1);
		}
	}

	@Override
	public void setAliveRegisters(Set<RegisterBundle> registers) {
		this.aliveRegisters.clear();
		this.aliveRegisters.addAll(registers);
	}
}
