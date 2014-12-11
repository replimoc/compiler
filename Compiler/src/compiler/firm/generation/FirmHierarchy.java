package compiler.firm.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.Declaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.type.BasicType;
import compiler.semantic.ClassScope;

import firm.ArrayType;
import firm.ClassType;
import firm.Entity;
import firm.MethodType;
import firm.Mode;
import firm.PointerType;
import firm.PrimitiveType;

/**
 * Methods to create and access entities
 */
public class FirmHierarchy {

	private final Mode modeInt = Mode.getIs(); // integer signed 32 bit
	private final Mode modeBoolean = Mode.getBu(); // unsigned 8 bit for boolean
	private final Mode modeReference = Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64); // 64 bit pointer

	private final Entity printInt;
	private final Entity calloc;

	private final HashMap<String, ClassWrapper> definedClasses = new HashMap<>();

	static class ClassWrapper {
		ClassWrapper(String className) {
			classType = new ClassType(className);
			referenceToClass = new PointerType(classType);
		}

		ClassType classType;
		firm.Type referenceToClass;
	}

	public FirmHierarchy() {
		// set 64bit pointer as default
		Mode.setDefaultModeP(getModeReference());

		// create library function(s)
		// void print_int(int);
		MethodType printIntType = new MethodType(new firm.Type[] { new PrimitiveType(getModeInt()) }, new firm.Type[] {});
		this.printInt = new Entity(firm.Program.getGlobalType(), "print_int", printIntType);

		// void* calloc_proxy (size_t num, size_t size);
		MethodType callocType = new MethodType(new firm.Type[] { new PrimitiveType(getModeInt()), new PrimitiveType(getModeInt()) },
				new firm.Type[] { new PrimitiveType(getModeReference()) });
		this.calloc = new Entity(firm.Program.getGlobalType(), "calloc_proxy", callocType);
	}

	public void initialize(HashMap<Symbol, ClassScope> classScopes) {
		// first iterate over all classes -- so that forward references to classes
		// in method parameters and return types work
		for (Entry<Symbol, ClassScope> currentEntry : classScopes.entrySet()) {
			String className = currentEntry.getKey().getValue();

			// Add class name
			ClassWrapper wrapper = new ClassWrapper(className);
			definedClasses.put(className, wrapper);
		}

		// iterate over all fields and methods and create firm entities
		for (Entry<Symbol, ClassScope> currentEntry : classScopes.entrySet()) {
			String className = currentEntry.getKey().getValue();
			ClassScope scope = currentEntry.getValue();

			// Create field declarations
			for (Declaration currentField : scope.getFieldDefinitions()) {
				new Entity(getClassType(className),
						currentField.getAssemblerName(),
						getTypeDeclaration(currentField.getType(), true));
			}
			for (MethodDeclaration currentMethod : scope.getMethodDefinitions()) {
				addMethodEntity(className, currentMethod);
			}

			ClassType classType = getClassEntity(className);
			classType.layoutFields();
			classType.finishLayout();
		}
	}

	private ClassType getClassType(String className) {
		return definedClasses.get(className).classType;
	}

	private void addMethodEntity(String className, MethodDeclaration methodDefinition) {
		ClassWrapper classWrapper = definedClasses.get(className);
		List<ParameterDefinition> parameterDefinitions = methodDefinition.getValidParameters();

		// types of parameters
		// first parameter is "this" with type referenceToClass
		firm.Type[] parameterTypes = new firm.Type[parameterDefinitions.size() + 1];
		parameterTypes[0] = classWrapper.referenceToClass;
		for (int paramIdx = 0; paramIdx < parameterDefinitions.size(); paramIdx++) {
			parameterTypes[paramIdx + 1] = getTypeDeclaration(parameterDefinitions.get(paramIdx).getType(), true);
		}

		// return type
		firm.Type[] returnType;
		if (methodDefinition.getType().is(BasicType.VOID)) {
			returnType = new firm.Type[] {};
		} else {
			returnType = new firm.Type[1];
			returnType[0] = getTypeDeclaration(methodDefinition.getType(), true);
		}

		// create methodType and methodEntity
		MethodType methodType = new MethodType(parameterTypes, returnType);

		// create new entity and attach to currentClass
		new Entity(classWrapper.classType, methodDefinition.getAssemblerName(), methodType);
	}

	public Entity getEntity(Declaration declaration) {
		return getClassType(declaration.getClassName()).getMemberByName(declaration.getAssemblerName());
	}

	public ClassType getClassEntity(String className) {
		return definedClasses.get(className).classType;
	}

	public firm.Type getTypeDeclaration(compiler.ast.type.Type type, boolean arrayAsReference) {

		firm.Type firmType = null;
		switch (type.getBasicType()) {
		case INT:
			firmType = new PrimitiveType(getModeInt());
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(getModeBoolean());
			break;
		case VOID:
			return null;
		case NULL:
			firmType = new PrimitiveType(getModeReference());
			break;
		case CLASS:
			firmType = definedClasses.get(type.getIdentifier().getValue()).referenceToClass;
			break;
		case ARRAY:
			if (arrayAsReference) {
				firmType = new PrimitiveType(getModeReference());
			} else {
				firmType = new ArrayType(getTypeDeclaration(type.getSubType(), true));
			}
			break;
		case METHOD:
			break;
		default:
			// type STRING_ARGS
			assert false;
		}

		return firmType;
	}

	public Entity getPrintInt() {
		return printInt;
	}

	public Entity getCalloc() {
		return calloc;
	}

	public Mode getModeInt() {
		return modeInt;
	}

	public Mode getModeBoolean() {
		return modeBoolean;
	}

	public Mode getModeReference() {
		return modeReference;
	}

}
