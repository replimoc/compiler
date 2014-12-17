package compiler.firm.backend.operations.general;

import compiler.firm.backend.operations.templates.AssemblerOperation;


public class TextOperation extends AssemblerOperation {

	@Override
	public String toString() {
		return "\t.text\n";
	}

}
