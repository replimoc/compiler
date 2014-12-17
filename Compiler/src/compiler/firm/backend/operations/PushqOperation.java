package compiler.firm.backend.operations;

import compiler.firm.backend.storage.Register;

public class PushqOperation extends AssemblerOperation {

	private final Register register;
	private final boolean oldContent;

	public PushqOperation(Register register, boolean oldContent) {
		this.register = register;
		this.oldContent = oldContent;
	}

	@Override
	public String toString() {
		String registerName = register.toString();
		if (oldContent)
			registerName = "(" + registerName + ")";
		return String.format("\tpushq %s\n", registerName);
	}

}
