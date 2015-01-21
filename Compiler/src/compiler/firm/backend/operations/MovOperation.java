package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public class MovOperation extends AssemblerBitOperation {

	private Storage source;
	private final Storage destination;

	public MovOperation(Storage source, Storage destination) {
		this(null, source, destination);
	}

	public MovOperation(String comment, Storage source, Storage destination) {
		super(comment);
		this.source = source;
		this.destination = destination;
	}

	@Override
	public String getOperationString() {
		Bit sourceMode = source.getMode();
		Bit destinationMode = destination.getMode();
		if (isMovslq(sourceMode, destinationMode)) {
			return String.format("\tmovs%s%s %s, %s", sourceMode, destinationMode, source.toString(), destination.toString());
		} else {
			Bit mode;

			if (sourceMode != null) {
				mode = sourceMode;
			} else if (destinationMode != null) {
				mode = destinationMode;
			} else {
				mode = Bit.BIT64;
			}

			return String.format("\tmov%s %s, %s", mode, source.toString(), destination.toString());
		}

	}

	private boolean isMovslq(Bit sourceMode, Bit destinationMode) {
		return sourceMode != null && destinationMode != null && sourceMode != destinationMode;
	}

	@Override
	public String[] toStringWithSpillcode() {
		Bit sourceMode = source.getMode();
		Bit destinationMode = destination.getMode();

		if (hasSpilledRegisters() && !isMovslq(sourceMode, destinationMode)) {
			if ((source.getClass() == VirtualRegister.class || source.getClass() == Constant.class)
					&& (destination.getClass() == VirtualRegister.class || destination.getClass() == Constant.class)) {

				if ((source.isSpilled() && !destination.isSpilled()) || (!source.isSpilled() && destination.isSpilled())) {
					return new String[] { toString() };
				} else {
					Storage oldSource = this.source;
					this.source = getTemporaryRegister().getRegister(destination.getMode());
					String[] result = new String[] {
							new MovOperation(oldSource, this.source).toString(),
							toString()
					};
					this.source = oldSource;
					return result;
				}
			}
		}
		return super.toStringWithSpillcode();
	}
}
