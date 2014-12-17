package compiler.firm.backend.operations.bit64;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public class RetOperation extends AssemblerOperation {

	@Override
	public String toString() {
		return "\tret\n";
	}

}
