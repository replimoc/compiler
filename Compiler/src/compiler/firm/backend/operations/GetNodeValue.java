package compiler.firm.backend.operations;

import compiler.firm.backend.Register;

public class GetNodeValue extends AssemblerOperation {

	private final Register destReg;
	private final int nodeOffset;

	public GetNodeValue(int nodeBPOffset, Register destRegister) {
		nodeOffset = nodeBPOffset;
		destReg = destRegister;
	}

	@Override
	public String toString() {
		return String.format("\tmovl %s(%rbp), %s\n", nodeOffset, destReg);
	}

}
