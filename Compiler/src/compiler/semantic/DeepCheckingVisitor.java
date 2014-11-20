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
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.TypeErrorException;
import compiler.semantic.exceptions.UndefinedSymbolException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.SymbolTable;

public class DeepCheckingVisitor implements AstVisitor {

	private final SymbolTable symbolTable;
	private final HashMap<Symbol, ClassScope> classScopes;
	private ClassScope currentClassScope = null;

	private List<SemanticAnalysisException> exceptions = new ArrayList<>();

	public DeepCheckingVisitor(HashMap<Symbol, ClassScope> classScopes) {
		this.classScopes = classScopes;
		this.symbolTable = new SymbolTable();
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	private void throwTypeError(AstNode astNode) {
		exceptions.add(new TypeErrorException(astNode.getPosition()));
	}

	private void throwRedefinitionError(Symbol symbol, Position definition, Position redefinition) {
		exceptions.add(new RedefinitionErrorException(symbol, definition, redefinition));
	}

	private void throwUndefinedSymbolError(Symbol symbol, Position position) {
		exceptions.add(new UndefinedSymbolException(symbol, position));
	}

	private void expectType(Type type, AstNode astNode) {
		if (!astNode.getType().equals(type)) {
			throwTypeError(astNode);
		}
	}

	private void expectType(BasicType type, AstNode astNode) {
		if (astNode.getType().getBasicType() != type ||
				astNode.getType().getSubType() != null) {
			throwTypeError(astNode);
		}
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
		if (!left.getType().equals(right.getType())) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		checkExpression(divisionExpression, BasicType.INT, BasicType.INT);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		checkBinaryOperandEquality(equalityExpression);
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
		checkBinaryOperandEquality(nonEqualityExpression);
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
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// first step in right [geklammerten] expression
		if  (variableAccessExpression.getExpression() != null) {
			variableAccessExpression.getExpression().accept(this);
		}
		
		// is inner expression (no left expression)
		if  (variableAccessExpression.getExpression() != null) {
			// variable can be defined in member scope or current class
			if (!variableAccessExpression.getFieldIdentifier().isDefined() &&
					currentClassScope.getFieldDefinition(variableAccessExpression.getFieldIdentifier()) == null) {
				throwUndefinedSymbolError(variableAccessExpression.getFieldIdentifier(), variableAccessExpression.getPosition());
				return;
			}
			// shouldn't be the type of variableAccessExpression set here?
		} else {
			Expression leftExpr = variableAccessExpression.getExpression();
			Type type = leftExpr.getType();
			// if left expression type is != class  (e.g. int, boolean, void) then throw error
			if (type.getBasicType() != BasicType.CLASS) {
				
			}
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ThisExpression thisExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NullExpression nullExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Type type) {
		type.setType(type);
	}

	@Override
	public void visit(Block block) {
		for (Statement statement : block.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
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
		visitMethodDeclaration(methodDeclaration);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		visitMethodDeclaration(staticMethodDeclaration);
	}

	private void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
		symbolTable.enterScope();

		for (ParameterDefinition parameterDefinition : methodDeclaration.getParameters()) {
			parameterDefinition.accept(this);
		}

		if (methodDeclaration.getBlock() != null) {
			methodDeclaration.getBlock().accept(this);
		}

		symbolTable.leaveScope();
		symbolTable.leaveAllScopes();
	}
}
