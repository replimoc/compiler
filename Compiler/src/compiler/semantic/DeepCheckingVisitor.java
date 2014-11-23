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
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.Statement;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.BinaryExpression;
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
import compiler.ast.statement.unary.UnaryExpression;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.InvalidMethodCallException;
import compiler.semantic.exceptions.NoSuchMemberException;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;
import compiler.semantic.exceptions.UndefinedSymbolException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;
import compiler.semantic.symbolTable.SymbolTable;

public class DeepCheckingVisitor implements AstVisitor {

	private final HashMap<Symbol, ClassScope> classScopes;

	private SymbolTable symbolTable = null;
	private Symbol currentClassSymbol = null;
	private ClassScope currentClassScope = null;
	private MethodDefinition currentMethodDefinition = null;

	private List<SemanticAnalysisException> exceptions = new ArrayList<>();
	private boolean isStaticMethod;

	public DeepCheckingVisitor(HashMap<Symbol, ClassScope> classScopes) {
		this.classScopes = classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	private void throwTypeError(AstNode astNode) {
		throwTypeError(astNode, null);
	}

	private void throwTypeError(AstNode astNode, String message) {
		exceptions.add(new TypeErrorException(astNode, message));
	}

	private void throwRedefinitionError(Symbol symbol, Position definition, Position redefinition) {
		exceptions.add(new RedefinitionErrorException(symbol, definition, redefinition));
	}

	private void throwUndefinedSymbolError(Symbol symbol, Position position) {
		exceptions.add(new UndefinedSymbolException(symbol, position));
	}

	private void throwNoSuchMemberError(Symbol object, Position objPos, Symbol member, Position memberPos) {
		exceptions.add(new NoSuchMemberException(object, objPos, member, memberPos));
	}

	private void expectType(Type type, AstNode astNode) {
		if (astNode.getType() != null
				&& !((type.getBasicType() != null && astNode.getType().getBasicType() == BasicType.NULL) || astNode.getType().equals(type))) {
			throwTypeError(astNode);
		}
	}

	private boolean expectType(BasicType type, AstNode astNode) {
		boolean result = true;
		if (astNode.getType() != null && (astNode.getType().getBasicType() != type || astNode.getType().getSubType() != null)) {
			throwTypeError(astNode);
			result = false;
		}
		return result;
	}

	private void setType(Type type, AstNode astNode) {
		astNode.setType(type);
	}

	private void setType(BasicType basicType, AstNode astNode) {
		setType(new Type(astNode.getPosition(), basicType), astNode);
	}

	private void checkBinaryOperandEquality(BinaryExpression binaryExpression) {
		AstNode left = binaryExpression.getOperand1();
		AstNode right = binaryExpression.getOperand2();
		left.accept(this);
		right.accept(this);
		if (left.getType() != null && right.getType() != null && !left.getType().equals(right.getType())) {
			throwTypeError(binaryExpression);
		}
	}

	private void checkBinaryOperandEqualityOrNull(BinaryExpression binaryExpression) {
		AstNode left = binaryExpression.getOperand1();
		AstNode right = binaryExpression.getOperand2();
		left.accept(this);
		right.accept(this);
		if (left.getType() != null
				&& right.getType() != null
				&& !(left.getType().equals(right.getType()) || left.getType().getBasicType() == BasicType.NULL || right.getType().getBasicType() == BasicType.NULL)) {
			throwTypeError(binaryExpression);
		}
	}

	private void checkExpression(BinaryExpression binaryExpression, BasicType expected, BasicType result) {
		checkBinaryOperandEquality(binaryExpression);
		expectType(expected, binaryExpression.getOperand1());
		setType(result, binaryExpression);
	}

	private void checkExpression(UnaryExpression unaryExpression, BasicType expected, BasicType result) {
		AstNode operand = unaryExpression.getOperand();
		operand.accept(this);
		expectType(expected, operand);
		setType(result, unaryExpression);
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		checkExpression(additionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		checkBinaryOperandEqualityOrNull(assignmentExpression);

		Expression operand1 = assignmentExpression.getOperand1();
		if (operand1 instanceof ThisExpression
				|| operand1 instanceof MethodInvocationExpression
				|| operand1 instanceof NewObjectExpression
				|| operand1 instanceof NewArrayExpression
				|| operand1 instanceof BinaryExpression
				|| operand1 instanceof LogicalNotExpression
				|| operand1 instanceof NegateExpression) {
			throwTypeError(assignmentExpression.getOperand1());
		}
		setType(operand1.getType(), assignmentExpression);
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		checkExpression(divisionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		checkBinaryOperandEqualityOrNull(equalityExpression);
		setType(BasicType.BOOLEAN, equalityExpression);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		checkExpression(greaterThanEqualExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		checkExpression(greaterThanExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		checkExpression(lessThanEqualExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		checkExpression(lessThanExpression, BasicType.INT, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		checkExpression(logicalAndExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		checkExpression(logicalOrExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		checkExpression(moduloExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		checkExpression(multiplicationExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		checkBinaryOperandEqualityOrNull(nonEqualityExpression);
		setType(BasicType.BOOLEAN, nonEqualityExpression);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		checkExpression(substractionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		setType(BasicType.BOOLEAN, booleanConstantExpression);
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		setType(BasicType.INT, integerConstantExpression);
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		newArrayExpression.getType().accept(this);
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		ClassType type = new ClassType(newObjectExpression.getPosition(), newObjectExpression.getIdentifier());
		visit(type);
		newObjectExpression.setType(type);
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// first step in outer left expression
		if (methodInvocationExpression.getMethodExpression() != null) {
			methodInvocationExpression.getMethodExpression().accept(this);
		}

		// is inner expression
		if (methodInvocationExpression.getMethodExpression() == null) {
			MethodDefinition methodDefinition = currentClassScope.getMethodDefinition(methodInvocationExpression.getMethodIdent());
			if (methodDefinition != null) {
				checkParameterDefinitionAndSetReturnType(methodInvocationExpression, methodDefinition);
			} else {
				throwNoSuchMemberError(currentClassSymbol, currentClassSymbol.getDefinition().getType().getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}
		} else {
			Expression leftExpression = methodInvocationExpression.getMethodExpression();

			Type leftExpressionType = leftExpression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != class (e.g. int, boolean, void) then throw error
			if (leftExpressionType.getBasicType() != BasicType.CLASS) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}

			// get class scope
			ClassScope classScope = classScopes.get(leftExpressionType.getIdentifier());
			// no need to check if it's null, as it has been checked before...

			MethodDefinition methodDefinition = classScope.getMethodDefinition(methodInvocationExpression.getMethodIdent());
			// is there the specified method?
			if (methodDefinition == null) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition());
				return;
			}
			checkParameterDefinitionAndSetReturnType(methodInvocationExpression, methodDefinition);
		}
	}

	private void checkParameterDefinitionAndSetReturnType(MethodInvocationExpression methodInvocationExpression, MethodDefinition methodDefinition) {
		// now check params
		if (methodDefinition.getParameters().length != methodInvocationExpression.getParameters().length) {
			exceptions.add(new InvalidMethodCallException(methodInvocationExpression.getMethodIdent(), methodInvocationExpression.getPosition()));
			return;
		}

		for (int i = 0; i < methodDefinition.getParameters().length; i++) {
			Definition parameterDefinition = methodDefinition.getParameters()[i];
			Expression expression = methodInvocationExpression.getParameters()[i];
			expression.accept(this);

			if (parameterDefinition.getType() != null) {
				expectType(parameterDefinition.getType(), expression);
			}
		}

		methodInvocationExpression.setType(methodDefinition.getType());

	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// first step in outer left expression
		if (variableAccessExpression.getExpression() != null) {
			variableAccessExpression.getExpression().accept(this);
		}

		// is inner expression (no left expression)
		if (variableAccessExpression.getExpression() == null) {
			// shouldn't be the type of variableAccessExpression set here?
			if (variableAccessExpression.getFieldIdentifier().isDefined()) {
				variableAccessExpression.setType(variableAccessExpression.getFieldIdentifier().getDefinition().getType());
			} else if (currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()) != null) {
				variableAccessExpression.setType(currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()).getType());
				// special case is System
			} else if (variableAccessExpression.getFieldIdentifier().getValue().equals("System")) {
				variableAccessExpression.setType(new ClassType(null, new Symbol("System")));
			} else {
				throwUndefinedSymbolError(variableAccessExpression.getFieldIdentifier(), variableAccessExpression.getPosition());
				return;
			}
		} else {
			Expression leftExpression = variableAccessExpression.getExpression();
			Type leftExpressionType = leftExpression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != class (e.g. int, boolean, void) then throw error
			if (leftExpressionType.getBasicType() != BasicType.CLASS) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						variableAccessExpression.getFieldIdentifier(),
						variableAccessExpression.getPosition());
				return;
			}
			// check if class exists
			ClassScope classScope = classScopes.get(leftExpressionType.getIdentifier());
			if (classScope == null) {
				throwTypeError(variableAccessExpression);
				return;
			}
			// check if member exists in this class
			Definition fieldDef = classScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier());
			if (fieldDef == null) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						variableAccessExpression.getFieldIdentifier(),
						variableAccessExpression.getPosition());
				return;
			}

			variableAccessExpression.setType(fieldDef.getType());
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		Expression arrayExpression = arrayAccessExpression.getArrayExpression();
		Expression indexExpression = arrayAccessExpression.getIndexExpression();
		arrayExpression.accept(this);
		indexExpression.accept(this);

		if (arrayExpression.getType() != null && arrayExpression.getType().getBasicType() == BasicType.ARRAY) {
			if (arrayExpression.getType().getSubType().getBasicType() == BasicType.STRING_ARGS) {
				throwTypeError(arrayAccessExpression, "Access on main method parameter forbidden.");
			}
		} else {
			throwTypeError(arrayAccessExpression, "Array access on a non array.");
		}
		expectType(BasicType.INT, indexExpression);
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		checkExpression(logicalNotExpression, BasicType.BOOLEAN, BasicType.BOOLEAN);
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		checkExpression(negateExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		if (returnStatement.getOperand() != null) {
			returnStatement.getOperand().accept(this);
			expectType(currentMethodDefinition.getType(), returnStatement.getOperand());
		} else if (currentMethodDefinition.getType().getBasicType() != BasicType.VOID) {
			throwTypeError(returnStatement);
		}
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		if (isStaticMethod) {
			throwTypeError(thisExpression, "'this' is not allowed in static methods.");
		}
		thisExpression.setType(new ClassType(null, currentClassSymbol));
	}

	@Override
	public void visit(NullExpression nullExpression) {
		setType(BasicType.NULL, nullExpression);
	}

	@Override
	public void visit(Type type) {
		type.setType(type);
	}

	@Override
	public void visit(ClassType classType) {
		if (classScopes.containsKey(classType.getIdentifier()) == false) {
			throwTypeError(classType); 
			return;
		}
	}

	@Override
	public void visit(Block block) {
		symbolTable.enterScope();
		for (Statement statement : block.getStatements()) {
			statement.accept(this);
		}
		symbolTable.leaveScope();
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentClassSymbol = classDeclaration.getIdentifier();
		currentClassScope = classScopes.get(classDeclaration.getIdentifier());

		for (ClassMember classMember : classDeclaration.getMembers()) {
			classMember.accept(this);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		AstNode condition = ifStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		if (ifStatement.getTrueCase() != null) {
			ifStatement.getTrueCase().accept(this);
		}
		if (ifStatement.getFalseCase() != null) {
			ifStatement.getFalseCase().accept(this);
		}
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		AstNode condition = whileStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		if (whileStatement.getBody() != null) {
			whileStatement.getBody().accept(this);
		}
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		if (symbolTable.isDefinedInCurrentScope(localVariableDeclaration.getIdentifier())) {
			throwRedefinitionError(localVariableDeclaration.getIdentifier(), null, localVariableDeclaration.getPosition());
			return;
		}
		symbolTable.insert(localVariableDeclaration.getIdentifier(), new Definition(localVariableDeclaration.getIdentifier(),
				localVariableDeclaration.getType()));

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			expression.accept(this);
			expectType(localVariableDeclaration.getType(), expression);
		}
	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		parameterDefinition.getType().accept(this);

		// check if parameter already defined
		if (symbolTable.isDefinedInCurrentScope(parameterDefinition.getIdentifier())) {
			throwRedefinitionError(parameterDefinition.getIdentifier(), null, parameterDefinition.getPosition());
			return;
		}
		symbolTable.insert(parameterDefinition.getIdentifier(), new Definition(parameterDefinition.getIdentifier(), parameterDefinition.getType()));
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration classDeclaration : program.getClasses()) {
			classDeclaration.accept(this);
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		isStaticMethod = false;
		visitMethodDeclaration(methodDeclaration);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		isStaticMethod = true;
		visitMethodDeclaration(staticMethodDeclaration);
	}

	private void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
		symbolTable = currentClassScope.createClassSymbolTable();

		for (ParameterDefinition parameterDefinition : methodDeclaration.getParameters()) {
			parameterDefinition.accept(this);
		}

		if (methodDeclaration.getBlock() != null) {
			currentMethodDefinition = currentClassScope.getMethodDefinition(methodDeclaration.getIdentifier());
			// don't visit block, as every block should have it's own scope
			for (Statement statement : methodDeclaration.getBlock().getStatements()) {
				statement.accept(this);
			}
			currentMethodDefinition = null;
		}

		symbolTable.leaveAllScopes();
		symbolTable = null;
	}
}
