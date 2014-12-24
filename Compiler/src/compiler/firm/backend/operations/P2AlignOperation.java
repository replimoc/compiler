package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class P2AlignOperation extends AssemblerOperation {

	@Override
	public String getOperationString() {
		return "\t.p2align 4,,15";
	}

}
