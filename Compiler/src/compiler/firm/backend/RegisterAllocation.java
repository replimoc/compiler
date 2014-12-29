package compiler.firm.backend;

import java.util.HashMap;
import java.util.List;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.StackPointer;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Phi;

public class RegisterAllocation {

	public static final int STACK_ITEM_SIZE = 8;

	private final List<AssemblerOperation> operations;
	private final HashMap<Node, Storage> nodeStorages = new HashMap<>();
	private int currentStackOffset;

	public RegisterAllocation(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite) {
		return getValue(node, registerOverwrite, null);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, Register register) {
		addOperation(new Comment("restore from stack"));

		// if variable was assigned, than simply load it from stack

		if (!nodeStorages.containsKey(node)) {
			// The value has not been set yet. Reserve memory for it. TODO: check if this is a valid case
			StackPointer stackOffset = reserveStackItem();
			addToNodeStorage(node, stackOffset);
			addOperation(new Comment("expected " + node + " to be on stack"));

		}
		return getValue(node, registerOverwrite, register, getStorage(node));
	}

	public Storage getStorage(Node node) {
		return nodeStorages.get(node);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, Storage stackPointer) {
		return getValue(node, registerOverwrite, null, stackPointer);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, RegisterBased register, Storage stackPointer) {
		register = getNewRegister(register);

		if (getMode(node) == Bit.BIT8) {
			addOperation(new MovOperation("movb does not clear the register before write", Bit.BIT64, new Constant(0), register));
		}

		addOperation(new MovOperation("Load address " + node.toString(), getMode(node), stackPointer, register));
		return register;
	}

	public StackPointer storeValueOnNewStackPointer(Node node, Storage storage) {
		// Allocate stack
		StackPointer stackPointer = reserveStackItem();

		storeValue(node, storage, stackPointer);
		return stackPointer;
	}

	private StackPointer reserveStackItem() {
		currentStackOffset -= STACK_ITEM_SIZE;
		return new StackPointer(currentStackOffset, Register._BP);
	}

	public void storeValue(Node node, Storage storage) {
		Storage stackPointer = nodeStorages.get(node);
		if (stackPointer == null) {
			stackPointer = storeValueOnNewStackPointer(node, storage);
			addToNodeStorage(node, stackPointer);
		} else {
			storeValue(node, storage, stackPointer);
		}
	}

	private void storeValue(Node node, Storage storage, Storage stackPointer) {
		addOperation(new MovOperation("Store node " + node, getMode(node), storage, stackPointer));
	}

	public void reserveMemoryForPhis(List<Phi> phis) {
		addOperation(new Comment("Reserve space for phis"));
		for (Phi phi : phis) {
			addToNodeStorage(phi, reserveStackItem());
		}
	}

	public Bit getMode(Node node) {
		if (node.getMode().equals(FirmUtils.getModeReference())) {
			return Bit.BIT64;
		} else if (node.getMode().equals(FirmUtils.getModeBoolean())) {
			return Bit.BIT8;
		} else {
			return Bit.BIT32;
		}
	}

	public void resetStackOffset() {
		currentStackOffset = 0;

	}

	public void addConstant(Const node) {
		addToNodeStorage(node, new Constant(node));
	}

	public void addToNodeStorage(Node node, Storage storage) {
		nodeStorages.put(node, storage);
	}

	public RegisterBased getNewRegister(RegisterBased register) {
		return new VirtualRegister(register);
	}

	public RegisterBased getNewRegister() {
		return new VirtualRegister();
	}

	public void freeRegister(RegisterBased register) {
	}
}
