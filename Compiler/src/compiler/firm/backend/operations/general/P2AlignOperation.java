package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;


public class P2AlignOperation extends AssemblerOperation {

	@Override
	public String toString() {
		return "\t.p2align 4,,15\n";
	}

}
