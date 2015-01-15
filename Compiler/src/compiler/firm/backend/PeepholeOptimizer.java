package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.DecOperation;
import compiler.firm.backend.operations.IncOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.LeaOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;

public class PeepholeOptimizer {

	private final Iterator<AssemblerOperation> inputOperations;
	private final List<AssemblerOperation> outputOperations;

	private AssemblerOperation currentOperation;

	public PeepholeOptimizer(ArrayList<AssemblerOperation> operations, List<AssemblerOperation> outputOperations) {
		this.inputOperations = operations.iterator();
		this.outputOperations = outputOperations;
		nextOperation();
	}

	private void nextOperation() {
		currentOperation = inputOperations.hasNext() ? inputOperations.next() : null;

		if (currentOperation instanceof Comment) {
			writeOperation();
			nextOperation();
		}
	}

	public void optimize() {
		while (currentOperation != null) {
			if (currentOperation instanceof JumpOperation) {
				JumpOperation jump = (JumpOperation) currentOperation;
				nextOperation();
				if (jump instanceof JmpOperation && currentOperation instanceof LabelOperation) {
					LabelOperation label = (LabelOperation) currentOperation;

					if (jump.getLabel().equals(label)) {
						writeOperation(new Comment(jump));
					} else {
						writeOperation(jump);
					}
				} else if (currentOperation instanceof JmpOperation) {
					JumpOperation jump2 = (JumpOperation) currentOperation;
					nextOperation();
					if (currentOperation instanceof LabelOperation) {
						LabelOperation label = (LabelOperation) currentOperation;

						if (jump2.getLabel().equals(label)) {
							writeOperation(jump);
							writeOperation(new Comment(jump2));
						} else if (jump.getLabel().equals(label) && !(jump instanceof JmpOperation)) {
							writeOperation(jump.invert(jump2.getLabel()));
							writeOperation(new Comment("Fallthrough to " + jump.getLabel()));
						} else {
							writeOperation(jump);
							writeOperation(jump2);
						}
					} else {
						writeOperation(jump);
						writeOperation(jump2);
					}
				} else {
					writeOperation(jump);
				}

			} else if (currentOperation instanceof SubOperation) {
				SubOperation sub = (SubOperation) currentOperation;
				Storage storage = sub.getStorage();
				if (storage instanceof Constant && ((Constant) storage).getConstant() == 1) {
					writeOperation(new DecOperation(sub.toString(), sub.getDestination()));
				} else {
					writeOperation();
				}
				nextOperation();

			} else if (currentOperation instanceof AddOperation) {
				AddOperation add = (AddOperation) currentOperation;
				Storage storage = add.getStorage();
				nextOperation();
				if (storage instanceof Constant && ((Constant) storage).getConstant() == 1) {
					writeOperation(new IncOperation(add.toString(), add.getDestination()));
				} else if (currentOperation instanceof MovOperation && add.getSource() instanceof RegisterBased) {
					MovOperation move = (MovOperation) currentOperation;
					if (move.getSource() == add.getDestination() && move.getDestination() instanceof RegisterBased
							&& !move.getDestination().isSpilled()) {
						writeOperation(new LeaOperation(new MemoryPointer((RegisterBased) add.getSource(), add.getDestination()),
								(RegisterBased) move.getDestination()));
						nextOperation();
					} else {
						writeOperation(add);
					}
				} else {
					writeOperation(add);
				}

			} else if (currentOperation instanceof MovOperation) {
				MovOperation move = (MovOperation) currentOperation;
				SingleRegister sourceRegister = move.getSource().getSingleRegister();
				SingleRegister destinationRegister = move.getDestination().getSingleRegister();

				if (sourceRegister != null && sourceRegister == destinationRegister) {
					writeOperation(new Comment(currentOperation)); // discard move between the same register
				} else {
					writeOperation();
				}
				nextOperation();

				// default case
			} else {
				writeOperation();
				nextOperation();
			}
		}
	}

	private void writeOperation(AssemblerOperation operation) {
		outputOperations.add(outputOperations.size(), operation);
	}

	private void writeOperation() {
		if (currentOperation != null)
			outputOperations.add(currentOperation);
	}
}
