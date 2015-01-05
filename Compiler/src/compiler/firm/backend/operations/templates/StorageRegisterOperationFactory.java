package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public interface StorageRegisterOperationFactory {
	StorageRegisterOperation instantiate(Storage storage, RegisterBased destination);
}
