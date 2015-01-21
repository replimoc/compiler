package compiler.firm.backend.operations.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class AssemblerBitOperation extends AssemblerOperation {

	public AssemblerBitOperation(String comment) {
		super(comment);
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			List<String> result = new ArrayList<>();
			result.add(new Comment("Operation with spill code").toString());
			HashMap<VirtualRegister, Storage> storageMapping = new HashMap<>();
			List<VirtualRegister> storageMappingWrite = new ArrayList<>();

			// unique read registers
			for (RegisterBased register : getReadRegisters()) {
				if (register.isSpilled()) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					Storage storage = insertSpillcode(virtualRegister, result, true);
					storageMapping.put(virtualRegister, storage);
				}
			}
			for (RegisterBased register : getWriteRegisters()) {
				if (register.isSpilled()) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					if (!storageMapping.containsKey(register)) {
						Storage storage = insertSpillcode(virtualRegister, result, false);
						storageMapping.put(virtualRegister, storage);
					}
					storageMappingWrite.add(virtualRegister);
				}
			}

			addOperation(result);

			for (Entry<VirtualRegister, Storage> storageMap : storageMapping.entrySet()) {
				VirtualRegister virtualRegister = storageMap.getKey();
				Storage stackPointer = storageMap.getValue();
				if (storageMappingWrite.contains(virtualRegister)) {
					MovOperation spillOperation = new MovOperation(virtualRegister, stackPointer);
					result.add(spillOperation.toString());
				}
				virtualRegister.setStorage(stackPointer);
			}

			String[] resultArray = new String[result.size()];
			result.toArray(resultArray);
			return resultArray;
		} else {
			List<String> result = new ArrayList<>();
			addOperation(result);

			String[] resultString = new String[result.size()];
			result.toArray(resultString);
			return resultString;
		}
	}

	private void addOperation(List<String> result) {
		AssemblerOperation preOperation = getPreOperation();
		if (preOperation != null)
			result.add(preOperation.toString());

		result.add(toString());

		AssemblerOperation postOperation = getPostOperation();
		if (postOperation != null)
			result.add(postOperation.toString());
	}

	protected MovOperation getPreOperation() {
		return null;
	}

	protected AssemblerOperation getPostOperation() {
		return null;
	}

	private Storage insertSpillcode(VirtualRegister virtualRegister, List<String> result, boolean restore) {
		Storage stackPointer = virtualRegister.getRegister();

		RegisterBundle temporaryRegisterBundle = getTemporaryRegister();
		SingleRegister temporaryRegister = temporaryRegisterBundle.getRegister(virtualRegister.getMode());

		if (restore) {
			MovOperation spillOperation = new MovOperation(stackPointer, temporaryRegister);
			result.add(spillOperation.toString());
		}
		virtualRegister.setStorage(temporaryRegister);

		return stackPointer;
	}
}
