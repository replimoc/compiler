package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterConstantOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;

public class ShrOperation extends RegisterConstantOperation {

    public ShrOperation(Bit mode, RegisterBased register, Constant constant) {
        super(mode, register, constant);
    }

    public ShrOperation(String comment, Bit mode, RegisterBased register, Constant constant) {
        super(comment, mode, register, constant);
    }

    @Override
    public String getOperationString() {
        return String.format("\tshr %s, %s",  getConstant(), getRegister());
    }
}
