package compiler.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.Declaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MemberDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.ast.declaration.NativeMethodDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.declaration.StaticMethodDeclaration;
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
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.IllegalAccessToNonStaticMemberException;
import compiler.semantic.exceptions.InvalidMethodCallException;
import compiler.semantic.exceptions.MainNotCallableException;
import compiler.semantic.exceptions.MissingReturnStatementOnAPathException;
import compiler.semantic.exceptions.NoSuchMemberException;
import compiler.semantic.exceptions.NotAnExpressionStatementException;
import compiler.semantic.exceptions.ReDeclarationErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;
import compiler.semantic.exceptions.UndefinedSymbolException;
import compiler.semantic.symbolTable.SymbolTable;

public class DeepCheckingVisitor implements AstVisitor {

	private final List<SemanticAnalysisException> exceptions = new ArrayList<>();
	private final HashMap<Symbol, ClassScope> classScopes;

	private SymbolTable symbolTable = null;
	private ClassDeclaration currentClassDeclaration;
	private ClassScope currentClassScope = null;
	private MethodMemberDeclaration currentMethodDeclaration = null;

	private boolean isStaticMethod;
	private boolean returnOnAllPaths;
	private boolean isExpressionStatement;

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

	private void throwReDeclarationError(Symbol symbol, Position reDeclarationPosition) {
		exceptions.add(new ReDeclarationErrorException(symbol, reDeclarationPosition));
	}

	private void throwUndefinedSymbolError(Symbol symbol, Position position) {
		exceptions.add(new UndefinedSymbolException(symbol, position));
	}

	private void throwNoSuchMemberError(Symbol object, Position objPos, Symbol member, Position memberPos) {
		exceptions.add(new NoSuchMemberException(object, objPos, member, memberPos));
	}

	private void throwIllegalAccessToNonStaticMemberError(Position objPos) {
		exceptions.add(new IllegalAccessToNonStaticMemberException(objPos));
	}

	private void throwMainNotCallableError(MethodInvocationExpression methodInvocation) {
		exceptions.add(new MainNotCallableException(methodInvocation));
	}

	private boolean expectType(Type type, AstNode astNode, boolean negate) {
		boolean correctType = (astNode.getType() == null || astNode.getType().equals(type));
		if (!negate && !correctType) {
			throwTypeError(astNode);
			return false;
		} else if (negate && correctType) {
			throwTypeError(astNode);
			return false;
		}
		return true;
	}

	private boolean expectType(Type type, AstNode astNode) {
		return expectType(type, astNode, false);
	}

	private boolean expectType(BasicType type, AstNode astNode) {
		return expectType(new Type(type), astNode, false);
	}

	private boolean expectTypeNot(BasicType type, AstNode astNode) {
		return expectType(new Type(type), astNode, true);
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

	private void checkExpression(BinaryExpression binaryExpression, BasicType expected, BasicType result) {
		checkBinaryOperandEquality(binaryExpression);
		expectType(expected, binaryExpression.getOperand1());
		binaryExpression.setType(result);
	}

	private void checkExpression(UnaryExpression unaryExpression, BasicType expected, BasicType result) {
		AstNode operand = unaryExpression.getOperand();
		operand.accept(this);
		expectType(expected, operand);
		unaryExpression.setType(result);
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		checkExpression(additionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		this.isExpressionStatement = true;

		checkBinaryOperandEquality(assignmentExpression);

		Expression operand1 = assignmentExpression.getOperand1();
		if (!(operand1 instanceof VariableAccessExpression || operand1 instanceof ArrayAccessExpression)) {
			throwTypeError(operand1, "Left side of assignment expression should variable or array.");
		}

		assignmentExpression.setType(operand1.getType());
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		checkExpression(divisionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		checkBinaryOperandEquality(equalityExpression);
		equalityExpression.setType(BasicType.BOOLEAN);
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
		checkBinaryOperandEquality(nonEqualityExpression);
		nonEqualityExpression.setType(BasicType.BOOLEAN);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		checkExpression(substractionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		booleanConstantExpression.setType(BasicType.BOOLEAN);
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		integerConstantExpression.setType(BasicType.INT);

		try {
			Integer.parseInt(integerConstantExpression.getIntegerLiteral());
		} catch (NumberFormatException exception) {
			throwTypeError(integerConstantExpression, "The integer is out of bounds.");
		}
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		newArrayExpression.getType().accept(this);
		newArrayExpression.getFirstDimension().accept(this);

		expectType(BasicType.INT, newArrayExpression.getFirstDimension());
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		ClassType type = new ClassType(newObjectExpression.getPosition(), newObjectExpression.getIdentifier());
		type.accept(this);
		if (classScopes.containsKey(newObjectExpression.getIdentifier())) {
			ClassScope objectClass = classScopes.get(newObjectExpression.getIdentifier());
			newObjectExpression.setObjectClass(objectClass.getClassDeclaration());
		}
		newObjectExpression.setType(type);
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// is inner expression
		if (methodInvocationExpression.getMethodExpression() == null) {
			if (isStaticMethod) { // there are no static methods
				throwIllegalAccessToNonStaticMemberError(methodInvocationExpression.getPosition());
				return;
			}
			checkCallMethod(methodInvocationExpression, currentClassScope);

		} else {
			// first step in outer left expression
			methodInvocationExpression.getMethodExpression().accept(this);

			Expression leftExpression = methodInvocationExpression.getMethodExpression();
			Type leftExpressionType = leftExpression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != BasicType.CLASS (e.g. int, boolean, void, array) throw error
			if (!leftExpressionType.is(BasicType.CLASS)) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(),
						leftExpressionType.getPosition(), methodInvocationExpression.getMethodIdentifier(), methodInvocationExpression.getPosition());
				return;
			}

			// get class scope
			ClassScope classScope = classScopes.get(leftExpressionType.getIdentifier());
			if (classScope == null) {
				throwUndefinedSymbolError(leftExpressionType.getIdentifier(), leftExpression.getPosition());
			} else {
				checkCallMethod(methodInvocationExpression, classScope);
			}
		}
	}

	private void checkCallMethod(MethodInvocationExpression methodInvocation, ClassScope classScope) {
		Symbol methodIdentifier = methodInvocation.getMethodIdentifier();
		MethodMemberDeclaration methodDeclaration = classScope.getMethodDeclaration(methodIdentifier);
		if (methodDeclaration == null) {
			throwNoSuchMemberError(currentClassDeclaration.getIdentifier(), currentClassDeclaration.getPosition(),
					methodIdentifier, methodInvocation.getPosition());
		} else if (methodDeclaration instanceof StaticMethodDeclaration) {
			throwMainNotCallableError(methodInvocation);
		} else {
			checkParameterDeclarationAndSetReturnType(methodInvocation, methodDeclaration);
		}
	}

	private void checkParameterDeclarationAndSetReturnType(MethodInvocationExpression methodInvocationExpression,
			MethodMemberDeclaration methodDeclaration) {
		// now check params
		if (methodDeclaration.getParameters().size() != methodInvocationExpression.getParameters().length) {
			exceptions
					.add(new InvalidMethodCallException(methodInvocationExpression.getMethodIdentifier(), methodInvocationExpression.getPosition()));
			return;
		}

		int i = 0;
		for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
			Expression expression = methodInvocationExpression.getParameters()[i];
			expression.accept(this);

			expectType(parameterDeclaration.getType(), expression);
			i++;
		}

		methodInvocationExpression.setType(methodDeclaration.getType());
		methodInvocationExpression.setMethodDeclaration(methodDeclaration);
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// first step in outer left expression
		Expression expression = variableAccessExpression.getExpression();

		// is inner expression (no left expression)
		if (expression == null) {
			Symbol fieldIdentifier = variableAccessExpression.getFieldIdentifier();
			Position position = variableAccessExpression.getPosition();
			Declaration declaration = null;
			if (fieldIdentifier.isDefined()) {
				if (isStaticMethod) {
					if (fieldIdentifier.getDeclarationScope().getParentScope() == null) {
						// class field has no parent scope since there are no inner classes
						// there are no static fields
						throwIllegalAccessToNonStaticMemberError(position);
						// continue
					}
				}
				declaration = fieldIdentifier.getDeclaration();
			} else if (currentClassScope.getFieldDeclaration(fieldIdentifier) != null) {
				// no static field defined in class scope possible
				if (isStaticMethod) {
					// there are no static fields
					throwIllegalAccessToNonStaticMemberError(position);
					// continue
				}
				declaration = currentClassScope.getFieldDeclaration(fieldIdentifier);
			} else if (classScopes.containsKey(fieldIdentifier)) {
				// Static access
				ClassScope staticScope = classScopes.get(fieldIdentifier);
				declaration = staticScope.getClassDeclaration();

				if (!staticScope.hasStaticField()) {
					throwUndefinedSymbolError(fieldIdentifier, position);
					return;
				}
			} else {
				throwUndefinedSymbolError(fieldIdentifier, position);
				return;
			}
			variableAccessExpression.setType(declaration.getType());
			variableAccessExpression.setDeclaration(declaration);
		} else {
			expression.accept(this);

			if (expression instanceof ThisExpression && isStaticMethod) {
				return;
			}

			Type leftExpressionType = expression.getType();

			if (leftExpressionType == null) {
				return; // left expressions failed...
			}

			// if left expression type is != class (e.g. int, boolean, void) then throw error
			if (!leftExpressionType.is(BasicType.CLASS)) {
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
			Declaration fieldDef = classScope.getFieldDeclaration(variableAccessExpression.getFieldIdentifier());
			if (fieldDef == null) {
				throwNoSuchMemberError(leftExpressionType.getIdentifier(), leftExpressionType.getPosition(),
						variableAccessExpression.getFieldIdentifier(),
						variableAccessExpression.getPosition());
				return;
			}

			variableAccessExpression.setDeclaration(fieldDef);
			variableAccessExpression.setType(fieldDef.getType());
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		Expression arrayExpression = arrayAccessExpression.getArrayExpression();
		Expression indexExpression = arrayAccessExpression.getIndexExpression();
		arrayExpression.accept(this);
		indexExpression.accept(this);

		if (arrayExpression.getType() == null) {
			return; // Already an error thrown in an upper left array part.
		}

		if (arrayExpression.getType().is(BasicType.ARRAY)) {
			if (arrayExpression.getType().getSubType().is(BasicType.STRING_ARGS)) {
				throwTypeError(arrayAccessExpression, "Access on main method parameter forbidden.");
			}
			arrayAccessExpression.setType(arrayExpression.getType().getSubType());
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
		isExpressionStatement = true;
		boolean isVoidMethod = currentMethodDeclaration.getType().is(BasicType.VOID);

		if (returnStatement.getOperand() != null) {
			if (isVoidMethod) {
				throwTypeError(returnStatement, "Expected return statement without type.");
				return;
			}
			returnStatement.getOperand().accept(this);
			expectType(currentMethodDeclaration.getType(), returnStatement.getOperand());
			returnOnAllPaths = true;
		} else if (!isVoidMethod) {
			throwTypeError(returnStatement);
		}
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		if (isStaticMethod) {
			throwTypeError(thisExpression, "'this' is not allowed in static methods.");
		}
		thisExpression.setType(new ClassType(null, currentClassDeclaration.getIdentifier()));
	}

	@Override
	public void visit(NullExpression nullExpression) {
		nullExpression.setType(BasicType.NULL);
	}

	@Override
	public void visit(Type type) {
		type.setType(type);
	}

	@Override
	public void visit(ClassType classType) {
		if (!classScopes.containsKey(classType.getIdentifier())) {
			throwTypeError(classType);
			return;
		}
		classType.setType(classType);
	}

	@Override
	public void visit(ArrayType arrayType) {
		Type finalSubtype = arrayType.getFinalSubtype();
		switch (finalSubtype.getBasicType()) {
		case VOID:
			throwTypeError(arrayType, "Can't create void arrays.");
			return;
		case CLASS:
			if (!classScopes.containsKey(arrayType.getIdentifier())) {
				throwTypeError(arrayType);
				return;
			}
			break;
		case ARRAY:
			throw new RuntimeException("Internal Compiler Error: This should never happen.");
		default:
			break;
		}

		arrayType.setType(arrayType);
	}

	@Override
	public void visit(Block block) {
		this.symbolTable.enterScope();
		boolean returnInBlockFound = returnOnAllPaths;

		for (Statement statement : block.getStatements()) {
			returnOnAllPaths = false;
			acceptStatement(statement);
			returnInBlockFound |= returnOnAllPaths;
		}

		returnOnAllPaths = returnInBlockFound;
		this.symbolTable.leaveScope();
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentClassDeclaration = classDeclaration;
		currentClassScope = classScopes.get(classDeclaration.getIdentifier());

		for (MemberDeclaration classMember : classDeclaration.getMembers()) {
			classMember.setClassDeclaration(classDeclaration);
			classMember.accept(this);
		}
		currentClassDeclaration = null;
	}

	@Override
	public void visit(IfStatement ifStatement) {
		AstNode condition = ifStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		boolean oldReturnOnAllPaths = returnOnAllPaths;
		boolean returnInTrueCase = false;
		boolean returnInFalseCase = false;

		Statement trueCase = ifStatement.getTrueCase();

		returnOnAllPaths = false;
		acceptStatement(trueCase);
		returnInTrueCase = returnOnAllPaths;

		Statement falseCase = ifStatement.getFalseCase();
		if (falseCase != null) {
			returnOnAllPaths = false;
			acceptStatement(falseCase);
			returnInFalseCase = returnOnAllPaths;
		}

		returnOnAllPaths = oldReturnOnAllPaths | (returnInTrueCase & returnInFalseCase);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		AstNode condition = whileStatement.getCondition();
		condition.accept(this);
		expectType(BasicType.BOOLEAN, condition);

		acceptStatement(whileStatement.getBody());
		returnOnAllPaths = false;
	}

	private void acceptStatement(Statement statement) {
		isExpressionStatement = false;

		statement.accept(this);

		if (statement instanceof Expression && !isExpressionStatement
				&& !(statement instanceof MethodInvocationExpression || statement instanceof NewObjectExpression)) {
			exceptions.add(new NotAnExpressionStatementException(statement.getPosition()));
		}
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		localVariableDeclaration.setClassDeclaration(currentClassDeclaration);
		localVariableDeclaration.getType().accept(this);

		expectTypeNot(BasicType.VOID, localVariableDeclaration);

		if (localVariableDeclaration.getIdentifier().isDefined()) {
			throwReDeclarationError(localVariableDeclaration.getIdentifier(), localVariableDeclaration.getPosition());
			return;
		}

		int variableNumber = symbolTable.insert(localVariableDeclaration.getIdentifier(), localVariableDeclaration.getType());
		localVariableDeclaration.setVariableNumber(variableNumber);

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			expression.accept(this);
			expectType(localVariableDeclaration.getType(), expression);
		}
	}

	@Override
	public void visit(ParameterDeclaration parameterDeclaration) {
		parameterDeclaration.setClassDeclaration(currentClassDeclaration);
		Type type = parameterDeclaration.getType();

		if (isStaticMethod) { // special case for String[] args
			type.setType(type);
		} else {
			type.accept(this);
		}

		expectTypeNot(BasicType.VOID, parameterDeclaration);

		// check if parameter already defined
		if (symbolTable.isDefinedInCurrentScope(parameterDeclaration.getIdentifier())) {
			throwReDeclarationError(parameterDeclaration.getIdentifier(), parameterDeclaration.getPosition());
			return;
		}
		int variableNumber = symbolTable.insert(parameterDeclaration.getIdentifier(), type);
		parameterDeclaration.setVariableNumber(variableNumber);
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
		fieldDeclaration.getType().accept(this);
		expectTypeNot(BasicType.VOID, fieldDeclaration);
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		isStaticMethod = true;
		visitMethodDeclaration(staticMethodDeclaration);
		isStaticMethod = false;
	}

	@Override
	public void visit(NativeMethodDeclaration nativeMethodDeclaration) {
		// nothing to do
	}

	private void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
		methodDeclaration.getType().accept(this);

		symbolTable = new SymbolTable();
		symbolTable.enterScope();

		for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
			parameterDeclaration.accept(this);
		}

		// visit body
		currentMethodDeclaration = currentClassScope.getMethodDeclaration(methodDeclaration.getIdentifier());
		if (currentMethodDeclaration != null) {
			returnOnAllPaths = false;
			methodDeclaration.getBlock().accept(this);

			// if method has return type, check if all paths have a return statement
			if (!currentMethodDeclaration.getType().is(BasicType.VOID)) {
				if (!returnOnAllPaths) {
					exceptions.add(new MissingReturnStatementOnAPathException(methodDeclaration.getPosition(), methodDeclaration.getIdentifier()));
				}
			}
		}

		// leave method scope.
		currentMethodDeclaration = null;
		symbolTable.leaveAllScopes();

		methodDeclaration.setNumberOfRequiredLocals(symbolTable.getRequiredLocalVariables());
		symbolTable = null;
	}

}
