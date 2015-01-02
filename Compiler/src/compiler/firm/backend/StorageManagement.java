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
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Phi;

public class StorageManagement {

	public static final int STACK_ITEM_SIZE = 8;

	private final List<AssemblerOperation> operations;
	private final HashMap<Node, Storage> nodeStorages = new HashMap<>();

	public StorageManagement(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	public void addStorage(Node node, Storage storage) {
		nodeStorages.put(node, storage);
	}

	public Storage getStorage(Node node) {
		return nodeStorages.get(node);
	}

	public void addConstant(Const node) {
		addStorage(node, new Constant(node));
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite) {
		return getValue(node, registerOverwrite, null);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, Register register) {
		addOperation(new Comment("restore from stack"));

		// if variable was assigned, than simply load it from stack

		if (!nodeStorages.containsKey(node)) {
			addStorage(node, new VirtualRegister());
			addOperation(new Comment("expected " + node + " to be on stack"));

		}
		return getValue(node, registerOverwrite, register, getStorage(node));
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, Storage originalStorage) {
		return getValue(node, registerOverwrite, null, originalStorage);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, RegisterBased register, RegisterBased originalStorage) {
		if (register == null && !registerOverwrite) {
			return originalStorage;
		} else {
			return getValueFromStorage(node, registerOverwrite, register, originalStorage);
		}
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, RegisterBased register, Storage originalStorage) {
		return getValueFromStorage(node, registerOverwrite, register, originalStorage);
	}

	public RegisterBased getValueFromStorage(Node node, boolean registerOverwrite, RegisterBased register, Storage originalStorage) {
		register = new VirtualRegister(register);

		if (getMode(node) == Bit.BIT8) {
			addOperation(new MovOperation("movb does not clear the register before write", Bit.BIT64, new Constant(0), register));
		}

		addOperation(new MovOperation("Load address " + node.toString(), getMode(node), originalStorage, register));
		return register;
	}

	public void storeValue(Node node, VirtualRegister storage) {
		Storage destination = nodeStorages.get(node);
		if (destination == null && storage.getRegister() == null) {
			addStorage(node, storage);
		} else {
			storeValueAndCreateNewStorage(node, storage);
		}
	}

	public void storeValue(Node node, Storage storage) {
		storeValueAndCreateNewStorage(node, storage);
	}

	public void storeValueAndCreateNewStorage(Node node, Storage storage) {
		Storage destination = nodeStorages.get(node);
		if (destination == null) {
			destination = new VirtualRegister();
			addStorage(node, destination);
		}
		storeValue(node, storage, destination);
	}

	public void storeValue(Node node, Storage storage, Storage destination) {
		if (storage != destination) {
			addOperation(new MovOperation("Store node " + node, getMode(node), storage, destination));
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

	public void reserveMemoryForPhis(List<Phi> phis) {
		addOperation(new Comment("Reserve space for phis"));
		for (Phi phi : phis) {
			addStorage(phi, new VirtualRegister());
		}
	}
}
