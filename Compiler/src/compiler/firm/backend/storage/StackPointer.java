package compiler.firm.backend.storage;

import compiler.firm.backend.operations.dummy.method.MethodStartEndOperation;

public class StackPointer extends MemoryPointer {

	private int temporaryStackOffset = 0;
	private MethodStartEndOperation methodStartEndOperation;

	public StackPointer(MethodStartEndOperation methodStartEndOperation, int offset) {
		super(offset, SingleRegister.RSP);

		this.methodStartEndOperation = methodStartEndOperation;
	}

	@Override
	public void setTemporaryStackOffset(int temporaryStackOffset) {
		this.temporaryStackOffset = temporaryStackOffset;
	}

	@Override
	public String toString() {
		int oldOffset = offset;
		offset += methodStartEndOperation.getStackOffset() + temporaryStackOffset;
		String result = super.toString();
		offset = oldOffset;
		return result;
	}
}
