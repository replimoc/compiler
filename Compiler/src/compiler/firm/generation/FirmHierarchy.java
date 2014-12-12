package compiler.firm.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.ClassDeclaration;
import compiler.ast.Declaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.type.BasicType;
import compiler.semantic.ClassScope;

import firm.ClassType;
import firm.Entity;
import firm.MethodType;
import firm.Mode;
import firm.PrimitiveType;

/**
 * Methods to create and access entities
 */
public class FirmHierarchy {

	private final Mode modeInt = Mode.getIs(); // integer signed 32 bit
	private final Mode modeBoolean = Mode.getBu(); // unsigned 8 bit for boolean
	private final Mode modeReference = Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64); // 64 bit pointer

	private final Entity calloc;

	private final HashMap<String, ClassWrapper> definedClasses = new HashMap<>();

	static class ClassWrapper {
		ClassWrapper(ClassDeclaration classDeclaration) {
			classType = classDeclaration.getType().getFirmClassType();
			referenceToClass = classDeclaration.getType().getFirmType();
		}

		ClassType classType;
		firm.Type referenceToClass;
	}

	public FirmHierarchy() {
		// set 64bit pointer as default
		Mode.setDefaultModeP(getModeReference());

		// create library function(s)
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
			ClassDeclaration classDeclaration = currentEntry.getValue().getClassDeclaration();

			// Add class name
			ClassWrapper wrapper = new ClassWrapper(classDeclaration);
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
						currentField.getType().getFirmType());
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
			parameterTypes[paramIdx + 1] = parameterDefinitions.get(paramIdx).getType().getFirmType();
		}

		// return type
		firm.Type[] returnType;
		if (methodDefinition.getType().is(BasicType.VOID)) {
			returnType = new firm.Type[] {};
		} else {
			returnType = new firm.Type[1];
			returnType[0] = methodDefinition.getType().getFirmType();
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

		return type.getFirmType();
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
