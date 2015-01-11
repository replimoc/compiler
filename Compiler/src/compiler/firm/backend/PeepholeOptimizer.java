package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;

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
			}

			writeOperation();
			nextOperation();
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
