package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.SourceDestinationOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public class MovOperation extends SourceDestinationOperation {

	public MovOperation(Bit mode, Storage source, Storage destination) {
		super(null, mode, source, destination);
	}

	public MovOperation(String comment, Bit mode, Storage source, Storage destination) {
		super(comment, mode, source, destination);
	}

	@Override
	public String getOperationString() {
		return createMov(getSource(), getDestination());
	}

	private String createMov(Storage source, Storage destination) {
		return String.format("\tmov%s %s, %s", getMode(), source.toString(getMode()), destination.toString(getMode()));
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			if (getSource().getClass() == VirtualRegister.class
					&& getDestination().getClass() == VirtualRegister.class) {
				VirtualRegister source = (VirtualRegister) getSource();
				VirtualRegister destination = (VirtualRegister) getDestination();

				if ((source.isSpilled() && !destination.isSpilled()) ||
						(!source.isSpilled() && destination.isSpilled())) {
					return new String[] { createMov(source.getRegister(), destination.getRegister()) };
				} else {
					Register temporaryRegister = getTemporaryRegister();
					return new String[] {
							createMov(source.getRegister(), temporaryRegister),
							createMov(temporaryRegister, destination.getRegister())
					};
				}
			}
		}
		return super.toStringWithSpillcode();
	}
}
