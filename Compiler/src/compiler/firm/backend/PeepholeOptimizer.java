package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.DecOperation;
import compiler.firm.backend.operations.IncOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;
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
			if (currentOperation instanceof JmpOperation) {
				JumpOperation jump = (JumpOperation) currentOperation;
				nextOperation();
				if (currentOperation instanceof LabelOperation) {
					LabelOperation label = (LabelOperation) currentOperation;

					if (jump.getLabel().equals(label)) {
						writeOperation(new Comment(jump));
					} else {
						writeOperation(jump);
					}
				} else {
					writeOperation(jump);
				}

			} else if (currentOperation instanceof SubOperation) {
				SubOperation sub = (SubOperation) currentOperation;
				Storage storage = sub.getStorage();
				if (storage instanceof Constant && ((Constant) storage).getConstant() == 1) {
					writeOperation(new DecOperation(sub.toString(), sub.getMode(), sub.getDestination()));
				} else {
					writeOperation();
				}
				nextOperation();

			} else if (currentOperation instanceof AddOperation) {
				AddOperation add = (AddOperation) currentOperation;
				Storage storage = add.getStorage();
				if (storage instanceof Constant && ((Constant) storage).getConstant() == 1) {
					writeOperation(new IncOperation(add.toString(), add.getMode(), add.getDestination()));
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
