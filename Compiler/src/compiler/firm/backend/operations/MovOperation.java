package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

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

		if (source instanceof VirtualRegister && destination instanceof VirtualRegister) {
			((VirtualRegister) destination).addPreferedRegister((VirtualRegister) source);
		}
	}

	@Override
	public String getOperationString() {
		Bit sourceMode = source.getMode();
		Bit destinationMode = getDestination().getMode();
		if (isMovslq(sourceMode, destinationMode)) {
			return String.format("\tmovs%s%s %s, %s", sourceMode, destinationMode, source.toString(), getDestination().toString());
		} else {
			Bit mode;

			if (sourceMode != null) {
				mode = sourceMode;
			} else if (destinationMode != null) {
				mode = destinationMode;
			} else {
				mode = Bit.BIT64;
			}

			return String.format("\tmov%s %s, %s", mode, source.toString(), getDestination().toString());
		}

	}

	private boolean isMovslq(Bit sourceMode, Bit destinationMode) {
		return sourceMode != null && destinationMode != null && sourceMode != destinationMode;
	}

	@Override
	public String[] toStringWithSpillcode() {
		Bit sourceMode = source.getMode();
		Bit destinationMode = getDestination().getMode();

		if (hasSpilledRegisters() && !isMovslq(sourceMode, destinationMode)) {
			if ((source.getClass() == VirtualRegister.class || source.getClass() == Constant.class)
					&& (getDestination().getClass() == VirtualRegister.class || getDestination().getClass() == Constant.class)) {

				if ((source.isSpilled() && !getDestination().isSpilled()) || (!source.isSpilled() && getDestination().isSpilled())) {
					return new String[] { toString() };
				} else {
					Storage oldSource = this.source;
					this.source = getSpillRegister().getRegister(getDestination().getMode());
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

	public Storage getSource() {
		return source;
	}

	public Storage getDestination() {
		return destination;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(source.getReadRegisters(), destination.getReadRegistersOnRightSide());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return destination.getWriteRegisters();
	}

}
