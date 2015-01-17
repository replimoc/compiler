package compiler.firm.backend.operations;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.RegisterOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

import java.util.Arrays;

public class OneOperandImulOperation extends RegisterOperation {

    private final VirtualRegister result_low = new VirtualRegister(Bit.BIT32, RegisterBundle._AX);
    private final VirtualRegister result_high = new VirtualRegister(Bit.BIT32, RegisterBundle._DX);

    public OneOperandImulOperation(String comment, RegisterBased register) {
        super(comment, register);
    }

    @Override
    public RegisterBased[] getReadRegisters() {
        RegisterBased[] usedRegister = getRegister().getUsedRegister();
        RegisterBased[] result = Arrays.copyOf(usedRegister, usedRegister.length + 2);
        result[usedRegister.length] = this.result_low;
        result[usedRegister.length + 1] = this.result_high;
        return result;
    }

    @Override
    public String getOperationString() {
        return String.format("\timul %s", getRegister().toString());
    }

    @Override
    public RegisterBased[] getWriteRegisters() {
        return new RegisterBased[] { result_low, result_high };
    }

    public VirtualRegister getResultLow() {
        return result_low;
    }

    public VirtualRegister getResultHigh() {
        return result_high;
    }

}
