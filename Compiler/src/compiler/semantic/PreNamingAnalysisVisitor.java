package compiler.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.Declaration;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.DivisionExpression;
import compiler.ast.statement.binary.EqualityExpression;
import compiler.ast.statement.binary.GreaterThanEqualExpression;
import compiler.ast.statement.binary.GreaterThanExpression;
import compiler.ast.statement.binary.LessThanEqualExpression;
import compiler.ast.statement.binary.LessThanExpression;
import compiler.ast.statement.binary.LogicalAndExpression;
import compiler.ast.statement.binary.LogicalOrExpression;
import compiler.ast.statement.binary.ModuloExpression;
import compiler.ast.statement.binary.MuliplicationExpression;
import compiler.ast.statement.binary.NonEqualityExpression;
import compiler.ast.statement.binary.SubtractionExpression;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.MultipleStaticMethodsException;
import compiler.semantic.exceptions.NoMainFoundException;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;

public class PreNamingAnalysisVisitor implements AstVisitor {

	private final HashMap<Symbol, ClassScope> classScopes = new HashMap<>();

	private HashMap<Symbol, Declaration> currentFieldsMap;
	private HashMap<Symbol, MethodDeclaration> currentMethodsMap;

	private boolean mainFound = false;
	private final List<SemanticAnalysisException> exceptions = new ArrayList<>();

	public HashMap<Symbol, ClassScope> getClassScopes() {
		return classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	public boolean hasMain() {
		return mainFound;
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration curr : program.getClasses()) {
			curr.accept(this);
		}

		if (!mainFound) {
			exceptions.add(new NoMainFoundException());
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentFieldsMap = new HashMap<>();
		currentMethodsMap = new HashMap<>();

		for (ClassMember curr : classDeclaration.getMembers()) {
			curr.accept(this);
		}

		Symbol identifier = classDeclaration.getIdentifier();
		if (classScopes.containsKey(identifier)) {
			throwRedefinitionError(identifier, classDeclaration.getPosition());
		} else {
			getClassScopes().put(identifier, new ClassScope(classDeclaration, currentFieldsMap, currentMethodsMap));
			currentFieldsMap = null;
			currentMethodsMap = null;
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		checkAndInsertDefinition(methodDeclaration);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		checkAndInsertDefinition(fieldDeclaration);
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		if (mainFound) {
			throwMultipleStaticMethodsError(staticMethodDeclaration.getPosition());
			return;
		}

		Type returnType = staticMethodDeclaration.getType();
		if (returnType.getBasicType() != BasicType.VOID) {
			throwTypeError(staticMethodDeclaration, "Invalid return type for main method.");
			return;
		}

		Symbol identifier = staticMethodDeclaration.getIdentifier();
		if (!"main".equals(identifier.getValue())) {
			throwTypeError(staticMethodDeclaration, "'public static void' method must be called 'main'.");
			return;
		}

		if (staticMethodDeclaration.getParameters().size() != 1) {
			throwTypeError(staticMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
			return;
		}

		ParameterDefinition parameter = staticMethodDeclaration.getParameters().get(0);
		Type parameterType = parameter.getType();
		if (parameterType.getBasicType() != BasicType.ARRAY || !"String".equals(parameterType.getSubType().getIdentifier().getValue())) {
			throwTypeError(staticMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
			return;
		}

		mainFound = true;
		staticMethodDeclaration.getParameters().get(0)
				.setType(new ArrayType(parameter.getPosition(), new Type(parameter.getPosition(), BasicType.STRING_ARGS)));
		checkAndInsertDefinition(staticMethodDeclaration);
	}

	private void checkAndInsertDefinition(MethodDeclaration definition) {
		if (currentMethodsMap.containsKey(definition.getIdentifier())) {
			throwRedefinitionError(definition.getIdentifier(), definition.getPosition());
			return;
		}

		currentMethodsMap.put(definition.getIdentifier(), definition);
	}

	private void checkAndInsertDefinition(FieldDeclaration definition) {
		if (currentFieldsMap.containsKey(definition.getIdentifier())) {
			throwRedefinitionError(definition.getIdentifier(), definition.getPosition());
			return;
		}

		currentFieldsMap.put(definition.getIdentifier(), definition);
	}

	private void throwTypeError(AstNode astNode, String message) {
		exceptions.add(new TypeErrorException(astNode, message));
	}

	private void throwRedefinitionError(Symbol identifier, Position redefinition) {
		exceptions.add(new RedefinitionErrorException(identifier, redefinition));
	}

	private void throwMultipleStaticMethodsError(Position definition) {
		exceptions.add(new MultipleStaticMethodsException(definition));
	}

	/*
	 * not needed visitor methods follow below
	 */

	@Override
	public void visit(AdditionExpression additionExpression) {
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
	}

	@Override
	public void visit(NegateExpression negateExpression) {
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
	}

	@Override
	public void visit(ThisExpression thisExpression) {
	}

	@Override
	public void visit(NullExpression nullExpression) {
	}

	@Override
	public void visit(Type type) {
	}

	@Override
	public void visit(ClassType classType) {
	}

	@Override
	public void visit(ArrayType arrayType) {
	}

	@Override
	public void visit(Block block) {
	}

	@Override
	public void visit(IfStatement ifStatement) {
	}

	@Override
	public void visit(WhileStatement whileStatement) {
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
	}

}
