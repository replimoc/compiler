package compiler.firm.backend;

import java.util.HashMap;
import java.util.List;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
import firm.nodes.Const;
import firm.nodes.Node;

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

	public void addConstant(Const node) {
		addStorage(node, new Constant(node));
	}

	public Storage getStorage(Node node) {
		Storage storage = nodeStorages.get(node);

		if (storage == null) {
			storage = new VirtualRegister(getMode(node));
			addStorage(node, storage);
		}
		return storage;
	}

	public RegisterBased getValue(Node node) {
		RegisterBased result;
		Storage storage = getStorage(node);
		if (storage instanceof RegisterBased) {
			result = (RegisterBased) storage;
		} else {
			result = new VirtualRegister(getMode(node));
			addOperation(new MovOperation(node.toString(), storage, result));
			addStorage(node, result);
		}
		return result;
	}

	public void storeValue(Node node, VirtualRegister storage) {
		Storage destination = nodeStorages.get(node);
		if (destination == null && storage.getRegister() == null) {
			addStorage(node, storage);
		} else {
			storeValueAndCreateNewStorage(node, storage, false);
		}
	}

	public void storeValue(Node node, Storage storage) {
		storeValueAndCreateNewStorage(node, storage, false);
	}

	public void storeValueAndCreateNewStorage(Node node, Storage storage, boolean forceNew) {
		Storage destination = nodeStorages.get(node);
		if (destination == null) {
			if (forceNew || countSuccessors(node) > 1) {
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
			addOperation(new MovOperation("Store node " + node, storage, destination));
		}
	}

	public static Bit getMode(Node node) {
		return getMode(node.getMode());
	}

	public static Bit getMode(Mode mode) {
		if (mode.equals(FirmUtils.getModeReference()) || mode.equals(Mode.getLu())) {
			return Bit.BIT64;
		} else if (mode.equals(FirmUtils.getModeBoolean())) {
			return Bit.BIT8;
		} else {
			return Bit.BIT32;
		}
	}

	private int countSuccessors(Node node) {
		int n = 0;
		for (Edge successors : BackEdges.getOuts(node)) {
			if (isNotModeM(successors.node)) {
				n++;
			}
		}
		return n;
	}

	private boolean isNotModeM(Node node) {
		return !node.getMode().equals(Mode.getM());
	}

	void storeToBackEdges(Node node, RegisterBased register) {
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				storeValueAndCreateNewStorage(edgeNode, register, true);
			}
		}
	}

	public void placeValue(Node node, RegisterBundle register) {
		addOperation(new MovOperation(getStorage(node), new VirtualRegister(StorageManagement.getMode(node), register)));

	}
}
