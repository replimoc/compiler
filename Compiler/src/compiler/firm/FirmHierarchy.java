package compiler.firm;

import java.util.HashMap;
import java.util.List;

import compiler.ast.ParameterDefinition;
import compiler.ast.type.BasicType;

import firm.ArrayType;
import firm.ClassType;
import firm.Entity;
import firm.MethodType;
import firm.Mode;
import firm.PointerType;
import firm.PrimitiveType;
import firm.Program;

/**
 * methods to create and access entities
 */
class FirmHierarchy {

	final Mode modeInt = Mode.getIs(); // integer signed 32 bit
	final Mode modeBool = Mode.getBu(); // unsigned 8 bit
	final Mode modeRef = Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64); // 64 bit pointer

	final Entity print_int;
	final Entity calloc;
	final Entity mainMethod;

	static class ClassWrapper {
		ClassWrapper(String className) {
			classType = new ClassType(className);
			refToClass = new PointerType(classType);
		}

		ClassType classType;
		firm.Type refToClass;
	}

	private ClassType currentClass = null;
	private firm.Type refToCurrentClass = null;
	private final HashMap<String, ClassWrapper> definedClasses = new HashMap<>();

	FirmHierarchy() {
		// set 64bit pointers as default
		Mode.setDefaultModeP(modeRef);

		// create library function(s)
		// void print_int(int);
		MethodType print_int_type = new MethodType(new firm.Type[] { new PrimitiveType(modeInt) }, new firm.Type[] {});
		this.print_int = new Entity(firm.Program.getGlobalType(), "#print_int", print_int_type);
		// void* calloc (size_t num, size_t size);
		MethodType calloc_type = new MethodType(new firm.Type[] { new PrimitiveType(modeInt), new PrimitiveType(modeInt) },
				new firm.Type[] { new PrimitiveType(modeRef) });
		this.calloc = new Entity(firm.Program.getGlobalType(), "#calloc", calloc_type);
		// void main(void)
		MethodType mainType = new MethodType(new firm.Type[] {}, new firm.Type[] {});
		this.mainMethod = new Entity(Program.getGlobalType(), "#main", mainType);
	}

	public void addClass(String className) {
		ClassWrapper wrapper = new ClassWrapper(className);
		definedClasses.put(className, wrapper);
	}

	public void setCurrentClass(String className) {
		ClassWrapper wrapper = definedClasses.get(className);
		currentClass = wrapper.classType;
		refToCurrentClass = wrapper.refToClass;
	}

	public void addFieldEntity(compiler.ast.type.Type fieldType, String fieldName) {
		firm.Type firmType = getTypeDeclaration(fieldType);
		String entityName = currentClass.getName() + "#" + "f" + "#" + fieldName;
		System.out.println("entityName = " + entityName);
		Entity entity = new Entity(currentClass, entityName, firmType);

		// entity.setLdIdent(entityName);
	}

	public void addMethodEntity(String methodName, List<ParameterDefinition> methodParams, compiler.ast.type.Type methodReturnType) {

		// types of parameters
		// first parameter is "this" with type referenceToClass
		firm.Type[] parameterTypes = new firm.Type[methodParams.size() + 1];
		parameterTypes[0] = refToCurrentClass;
		for (int paramNum = 0; paramNum < methodParams.size(); paramNum++) {
			ParameterDefinition parameterDefinition = methodParams.get(paramNum);
			parameterTypes[paramNum + 1] = getTypeDeclaration(parameterDefinition.getType());
		}

		// return type
		firm.Type[] returnType;
		if (methodReturnType.getBasicType() == BasicType.VOID) {
			returnType = new firm.Type[] {};
		} else {
			returnType = new firm.Type[1];
			returnType[0] = getTypeDeclaration(methodReturnType);
		}

		// create methodType and methodEntity
		MethodType methodType = new MethodType(parameterTypes, returnType);
		String entityName = currentClass.getName() + "#" + "m" + "#" + methodName;
		System.out.println("entityName = " + entityName);
		Entity methodEntity = new Entity(currentClass, entityName, methodType);
		// entity.setLdIdent(entityName);
	}

	public Entity getMethodEntity(String methodName) {
		String entityName = currentClass.getName() + "#" + "m" + "#" + methodName;
		Entity method = currentClass.getMemberByName(entityName);
		System.out.println("method = " + method);
		return method;
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
			firmType = new PrimitiveType(modeInt);
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(modeBool);
			break;
		case VOID:
			// TODO: return null?
			break;
		case NULL:
			firmType = new PrimitiveType(modeRef);
			break;
		case CLASS:
			firmType = definedClasses.get(type.getIdentifier().getValue()).refToClass;
			break;
		case METHOD:
			break;
		default:
			assert false;
		}

		if (type.getBasicType() == BasicType.ARRAY) {
			// create composite type
			firmType = new ArrayType(firmType);
		}

		return firmType;
	}

}
