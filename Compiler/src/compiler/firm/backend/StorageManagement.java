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
import firm.nodes.Conv;
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

	public Storage getValueAvoidNewRegister(Node node) {
		if (nodeStorages.containsKey(node)) {
			return nodeStorages.get(node);
		} else {
			return getValue(node, false);
		}
	}

	public RegisterBased getValue(Node node, boolean overwrite) {
		return getValue(node, null, overwrite);
	}

	public RegisterBased getValue(Node node, RegisterBundle resultRegisterBundle) {
		return getValue(node, resultRegisterBundle.getRegister(getMode(node)), true);
	}

	private RegisterBased getValue(Node node, RegisterBased resultRegister, boolean overwrite) {
		if (!nodeStorages.containsKey(node)) {
			addStorage(node, new VirtualRegister(getMode(node)));
		}
		Storage originalStorage = nodeStorages.get(node);

		if (!overwrite && resultRegister == null && originalStorage.getClass() == VirtualRegister.class && !(node instanceof Conv)) {
			return (VirtualRegister) originalStorage;
		}

		Bit mode = getMode(node);
		resultRegister = new VirtualRegister(mode, resultRegister);

		addOperation(new MovOperation("Load address " + node.toString(), originalStorage, resultRegister));
		return resultRegister;
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

	public void reserveMemoryForPhis(List<Phi> phis) {
		for (Phi phi : phis) {
			addStorage(phi, new VirtualRegister(getMode(phi)));
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
}
