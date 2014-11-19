package compiler.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
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
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.NoMainFoundException;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;

public class PreNamingAnalysisVisitor implements AstVisitor {

	private final HashMap<Symbol, ClassScope> classScopes = new HashMap<>();
	private HashMap<Symbol, Definition> currentFieldsMap;
	private HashMap<Symbol, MethodDefinition> currentMethodsMap;

	private boolean mainFound = false;
	private List<SemanticAnalysisException> exceptions = new ArrayList<>();

	public HashMap<Symbol, ClassScope> getClassScopes() {
		return classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
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

		getClassScopes().put(classDeclaration.getIdentifier(), new ClassScope(currentFieldsMap, currentMethodsMap));
		currentFieldsMap = null;
		currentMethodsMap = null;
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		Symbol identifier = methodDeclaration.getIdentifier();
		Type returnType = methodDeclaration.getType();
		List<ParameterDefinition> parameters = methodDeclaration.getParameters();

		Definition[] parameterDefinitions = new Definition[parameters.size()];

		int i = 0;
		for (ParameterDefinition currParameter : methodDeclaration.getParameters()) {
			parameterDefinitions[i] = new Definition(currParameter.getIdentifier(), currParameter.getType());
			i++;
		}

		MethodDefinition methodDefinition = new MethodDefinition(identifier, returnType, parameterDefinitions);

		checkAndInsertDefinition(methodDefinition, methodDeclaration.getPosition());
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		checkAndInsertDefinition(new Definition(fieldDeclaration.getIdentifier(), fieldDeclaration.getType()), fieldDeclaration.getPosition());
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		Type returnType = staticMethodDeclaration.getType();
		if (returnType.getBasicType() != BasicType.VOID) {
			throwTypeError(staticMethodDeclaration, "Invalid return type for main method.");
			return;
		}

		Symbol identifier = staticMethodDeclaration.getIdentifier();
		if (!"main".equals(identifier.getValue())) {
			throwTypeError(staticMethodDeclaration, "'public static void' method must be called 'main'.");
		}

		if (staticMethodDeclaration.getParameters().size() != 1) {
			throwTypeError(staticMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
		}

		ParameterDefinition parameter = staticMethodDeclaration.getParameters().get(0);
		Type parameterType = parameter.getType();
		if (parameterType.getBasicType() != BasicType.ARRAY || !"String".equals(parameterType.getSubType().getIdentifier().getValue())) {
			throwTypeError(staticMethodDeclaration, "'public static void main' method must have a single argument of type String[].");
		}

		mainFound = true;
		Position position = staticMethodDeclaration.getPosition();

		Definition[] parameterTypes = new Definition[] { new Definition(parameter.getIdentifier(),
				new Type(parameter.getPosition(), BasicType.STRING_ARGS)) };
		checkAndInsertDefinition(new MethodDefinition(identifier, returnType, parameterTypes), position);
	}

	private void checkAndInsertDefinition(MethodDefinition definition, Position position) {
		if (currentMethodsMap.containsKey(definition.getSymbol())) {
			throwRedefinitionError(definition.getSymbol(), null, position);
			return;
		}

		currentMethodsMap.put(definition.getSymbol(), definition);
	}

	private void checkAndInsertDefinition(Definition definition, Position position) {
		if (currentFieldsMap.containsKey(definition.getSymbol())) {
			throwRedefinitionError(definition.getSymbol(), null, position);
			return;
		}

		currentFieldsMap.put(definition.getSymbol(), definition);
	}

	private void throwTypeError(AstNode astNode, String message) {
		exceptions.add(new TypeErrorException(astNode.getPosition(), message));
	}

	private void throwRedefinitionError(Symbol identifier, Position definition, Position redefinition) {
		exceptions.add(new RedefinitionErrorException(identifier, definition, redefinition));
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
