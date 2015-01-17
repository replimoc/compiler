package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterConstantOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public class SarOperation extends RegisterConstantOperation {

    public SarOperation(Bit mode, RegisterBased register, Constant constant) {
        super(mode, register, constant);
    }

    public SarOperation(String comment, Bit mode, RegisterBased register, Constant constant) {
        super(comment, mode, register, constant);
    }

    @Override
    public String getOperationString() {
        return String.format("\tsar %s, %s",  getConstant(), getRegister());
    }
}
