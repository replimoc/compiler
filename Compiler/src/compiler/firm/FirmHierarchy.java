package compiler.firm;

import java.util.HashMap;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.type.BasicType;
import compiler.semantic.ClassScope;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;

import firm.ArrayType;
import firm.ClassType;
import firm.Entity;
import firm.MethodType;
import firm.Mode;
import firm.PointerType;
import firm.PrimitiveType;
import firm.Program;

/**
 * Methods to create and access entities
 */
class FirmHierarchy {

	private final Mode modeInt = Mode.getIs(); // integer signed 32 bit
	private final Mode modeBool = Mode.getBu(); // unsigned 8 bit for boolean
	private final Mode modeRef = Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64); // 64 bit pointer

	private final Entity print_int;
	private final Entity calloc;
	private final Entity mainMethod;

	private final HashMap<String, ClassWrapper> definedClasses = new HashMap<>();

	static class ClassWrapper {
		ClassWrapper(String className) {
			classType = new ClassType(className);
			refToClass = new PointerType(classType);
		}

		ClassType classType;
		firm.Type refToClass;
	}

	FirmHierarchy() {
		// set 64bit pointer as default
		Mode.setDefaultModeP(getModeRef());

		// create library function(s)
		// void print_int(int);
		MethodType print_int_type = new MethodType(new firm.Type[] { new PrimitiveType(getModeInt()) }, new firm.Type[] {});
		this.print_int = new Entity(firm.Program.getGlobalType(), "#print_int", print_int_type);

		// void* calloc (size_t num, size_t size);
		MethodType calloc_type = new MethodType(new firm.Type[] { new PrimitiveType(getModeInt()), new PrimitiveType(getModeInt()) },
				new firm.Type[] { new PrimitiveType(getModeRef()) });
		this.calloc = new Entity(firm.Program.getGlobalType(), "#calloc", calloc_type);

		// void main(void)
		MethodType mainType = new MethodType(new firm.Type[] {}, new firm.Type[] {});
		this.mainMethod = new Entity(Program.getGlobalType(), "#main", mainType);
	}

	public void initialize(HashMap<Symbol, ClassScope> classScopes) {
		for (Entry<Symbol, ClassScope> currEntry : classScopes.entrySet()) {
			String className = currEntry.getKey().getValue();
			addClass(className);
			ClassScope scope = currEntry.getValue();

			for (Definition currField : scope.getFieldDefinitions()) {
				addFieldEntity(className, currField);
			}
			for (MethodDefinition currMethod : scope.getMethodDefinitions()) {
				addMethodEntity(className, currMethod);
			}
		}
	}

	private void addClass(String className) {
		ClassWrapper wrapper = new ClassWrapper(className);
		definedClasses.put(className, wrapper);
	}

	private void addFieldEntity(String className, Definition definition) {
		firm.Type firmType = getTypeDeclaration(definition.getType());
		String entityName = className + "#" + "f" + "#" + definition.getSymbol().getValue();
		System.out.println("entityName = " + entityName);

		// create new entity and attach to currentClass
		new Entity(getClassType(className), entityName, firmType);
	}

	private ClassType getClassType(String className) {
		return definedClasses.get(className).classType;
	}

	private void addMethodEntity(String className, MethodDefinition methodDefinition) {
		ClassWrapper classWrapper = definedClasses.get(className);
		Definition[] parameterDefinitions = methodDefinition.getParameters();

		// types of parameters
		// first parameter is "this" with type referenceToClass
		firm.Type[] parameterTypes = new firm.Type[parameterDefinitions.length + 1];
		parameterTypes[0] = classWrapper.refToClass;
		for (int paramIdx = 0; paramIdx < parameterDefinitions.length; paramIdx++) {
			parameterTypes[paramIdx + 1] = getTypeDeclaration(parameterDefinitions[paramIdx].getType());
		}

		// return type
		firm.Type[] returnType;
		if (methodDefinition.getType().getBasicType() == BasicType.VOID) {
			returnType = new firm.Type[] {};
		} else {
			returnType = new firm.Type[1];
			returnType[0] = getTypeDeclaration(methodDefinition.getType());
		}

		// create methodType and methodEntity
		MethodType methodType = new MethodType(parameterTypes, returnType);
		String entityName = className + "#" + "m" + "#" + methodDefinition.getSymbol().getValue();
		System.out.println("entityName = " + entityName);

		// create new entity and attach to currentClass
		new Entity(classWrapper.classType, entityName, methodType);
	}

	public Entity getMethodEntity(String className, String methodName) {
		String entityName = className + "#" + "m" + "#" + methodName;
		Entity method = getClassType(className).getMemberByName(entityName);
		System.out.println("method = " + method);
		return method;
	}

	public Entity getFieldEntity(String fieldName) {
		String entityName = currentClass.getName() + "#f#" + fieldName;
		Entity field = currentClass.getMemberByName(entityName);
		System.out.println("field = " + field);
		return field;
	}

	public ClassType getClassEntity(String className) {
		return definedClasses.get(className).classType;
	}

	public firm.Type getType(compiler.ast.type.Type type) {
		return getTypeDeclaration(type);
	}

	private firm.Type getTypeDeclaration(compiler.ast.type.Type type) {
		compiler.ast.type.Type tmpType = type;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}

		firm.Type firmType = null;
		switch (tmpType.getBasicType()) {
		case INT:
			firmType = new PrimitiveType(getModeInt());
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(getModeBool());
			break;
		case VOID:
			return null;
		case NULL:
			firmType = new PrimitiveType(getModeRef());
			break;
		case CLASS:
			firmType = definedClasses.get(type.getIdentifier().getValue()).refToClass;
			break;
		case METHOD:
			break;
		default:
			// type STRING_ARGS
			assert false;
		}

		if (type.getBasicType() == BasicType.ARRAY) {
			// create composite type
			firmType = new ArrayType(firmType);
		}

		return firmType;
	}

	public Entity getPrint_int() {
		return print_int;
	}

	public Entity getCalloc() {
		return calloc;
	}

	public Entity getMainMethod() {
		return mainMethod;
	}

	public Mode getModeInt() {
		return modeInt;
	}

	public Mode getModeBool() {
		return modeBool;
	}

	public Mode getModeRef() {
		return modeRef;
	}

}
