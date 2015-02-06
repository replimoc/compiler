package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.TestOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;

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
							setAlignment(jump);
							writeOperation(jump);
							writeOperation(new Comment(jump2));
						} else if (jump.getLabel().equals(label) && !(jump instanceof JmpOperation)) {
							setAlignment(jump2);
							writeOperation(jump.invert(jump2.getLabel()));
							writeOperation(new Comment("Fallthrough to " + jump.getLabel()));
						} else {
							setAlignment(jump);
							setAlignment(jump2);
							writeOperation(jump);
							writeOperation(jump2);
						}
					} else {
						setAlignment(jump);
						setAlignment(jump2);
						writeOperation(jump);
						writeOperation(jump2);
					}
				} else {
					setAlignment(jump);
					writeOperation(jump);
				}
			} else if (currentOperation instanceof CmpOperation &&
					((CmpOperation) currentOperation).getSource1() instanceof Constant) {
				CmpOperation cmp = (CmpOperation) currentOperation;
				Constant constant = (Constant) cmp.getSource1();
				if (constant.getConstant() == 0) {
					writeOperation(new TestOperation(cmp.toString(), cmp.getSource2(), cmp.getSource2()));
				} else if (constant.getConstant() == 0xff && cmp.getSource2().getMode().equals(Bit.BIT8)) {
					nextOperation();
					if (currentOperation instanceof JumpOperation) {
						// FIXME: This is only valid if Bit.BIT8 is used for booleans
						writeOperation(new TestOperation(cmp.toString(), cmp.getSource2(), cmp.getSource2()));
						JumpOperation jump = (JumpOperation) currentOperation;
						currentOperation = jump.invert(jump.getLabel());
						continue;
					} else {
						writeOperation(cmp);
						continue; // Next operation already executed
					}
				} else {
					writeOperation();
				}
				nextOperation();
			} else { // default case
				writeOperation();
				nextOperation();
			}
		}
	}

	private void setAlignment(JumpOperation jump) {
		if (outputOperations.contains(jump.getLabel())) {
			jump.getLabel().setAlign(true);
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
