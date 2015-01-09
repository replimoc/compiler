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

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
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

	public Storage getValueAvoidNewRegister(Node node, boolean registerOverwrite) {
		if (!registerOverwrite && nodeStorages.containsKey(node)) {
			return nodeStorages.get(node);
		} else {
			return getValue(node, registerOverwrite);
		}
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite) {
		return getValue(node, null);
	}

	public RegisterBased getValue(Node node, Register register) {
		addOperation(new Comment("restore from stack"));

		// if variable was assigned, than simply load it from stack

		if (!nodeStorages.containsKey(node)) {
			addStorage(node, new VirtualRegister(getMode(node)));
			addOperation(new Comment("expected " + node + " to be on stack"));
		}

		return getValue(node, register, getStorage(node));
	}

	public RegisterBased getValue(Node node, boolean registerOverride, Storage originalStorage) {
		return getValue(node, null, originalStorage);
	}

	public RegisterBased getValue(Node node, boolean registerOverwrite, RegisterBased register, RegisterBased originalStorage) {
		if (register == null && !registerOverwrite) {
			return originalStorage;
		} else {
			return getValueFromStorage(node, register, originalStorage);
		}
	}

	public RegisterBased getValue(Node node, RegisterBased register, Storage originalStorage) {
		return getValueFromStorage(node, register, originalStorage);
	}

	public RegisterBased getValueFromStorage(Node node, RegisterBased register, Storage originalStorage) {
		Bit mode = getMode(node);
		register = new VirtualRegister(mode, register);

		addOperation(new MovOperation("Load address " + node.toString(), mode, originalStorage, register));
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
			if (newRegisterIsNecessary(node)) {
				destination = new VirtualRegister(getMode(node));
			} else {
				destination = storage;
			}
			addStorage(node, destination);
		}
		storeValue(node, storage, destination);
	}

	public void storeValue(Node node, Storage storage, Storage destination) {
		if (storage != destination) {
			addOperation(new MovOperation("Store node " + node, getMode(node), storage, destination));
		}
	}

	public static Bit getMode(Node node) {
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
			addStorage(phi, new VirtualRegister(getMode(phi)));
		}
	}

	private boolean newRegisterIsNecessary(Node node) {
		boolean result = true;
		for (Node predecessor : node.getPreds()) {
			if (isNotModeM(predecessor)) {
				int n = 0;
				for (Edge predecessorSuccessors : BackEdges.getOuts(predecessor)) {
					if (isNotModeM(predecessorSuccessors.node)) {
						n++;
					}
				}
				result &= (n <= 1);
			}
		}

		return result;
	}

	private boolean isNotModeM(Node node) {
		return !node.getMode().equals(Mode.getM());
	}
}
