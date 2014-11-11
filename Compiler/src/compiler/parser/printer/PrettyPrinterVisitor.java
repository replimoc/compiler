package compiler.parser.printer;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.ParameterDefinition;
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
import compiler.ast.statement.StringLiteral;
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
import compiler.ast.statement.type.Type;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.TokenType;

public class PrettyPrinterVisitor implements AstVisitor {
	private String outputString = "";

	/**
	 * Gets the result of the PrettyPrinterVisitor
	 * 
	 * @return
	 */
	public String getOutputString() {
		return this.outputString;
	}

	/**
	 * Visit a binary expression, use the TokenType to display the symbol and show the brackets.
	 * 
	 * @param binaryExpression
	 *            Expression to display
	 * @param tokenType
	 *            Type of the token.
	 */
	private void visit(BinaryExpression binaryExpression, TokenType tokenType) {
		// TODO Show only brackets if it is necessary.
		outputString += "(";
		binaryExpression.getOperand1().accept(this);
		outputString += tokenType.getString();
		binaryExpression.getOperand2().accept(this);
		outputString += ")";
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		visit(additionExpression, TokenType.ADD);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		visit(assignmentExpression, TokenType.ASSIGN);
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		visit(divisionExpression, TokenType.DIVIDE);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		visit(equalityExpression, TokenType.EQUAL);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		visit(greaterThanEqualExpression, TokenType.GREATEREQUAL);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		visit(greaterThanExpression, TokenType.GREATER);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		visit(lessThanEqualExpression, TokenType.LESSEQUAL);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		visit(lessThanExpression, TokenType.LESS);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		visit(logicalAndExpression, TokenType.LOGICALAND);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		visit(logicalOrExpression, TokenType.LOGICALOR);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		visit(moduloExpression, TokenType.MODULO);
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		visit(multiplicationExpression, TokenType.MULTIPLY);
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		visit(nonEqualityExpression, TokenType.NOTEQUAL);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		visit(substractionExpression, TokenType.SUBTRACT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		outputString += booleanConstantExpression.isValue();
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		outputString += integerConstantExpression.getIntegerLiteral();
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// TODO: right format!
		// outputString += "(";

		// expr == null --> this.method()
		// otherwise expr.ident()
		if (!methodInvocationExpression.isLocalMethod()) {
			methodInvocationExpression.getMethodExpression().accept(this);
		}
		// outputString += ")";
		outputString += "." + methodInvocationExpression.getMethodIdent() + "(";
		Expression[] args = methodInvocationExpression.getParameters();

		// print args
		if (args != null && args.length > 0) {
			int i = 0;
			args[i++].accept(this);
			while (i < args.length) {
				outputString += ", ";
				args[i++].accept(this);
			}
		}

		outputString += ")";
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		// TODO: right format!
		outputString += "(new ";
		newArrayExpression.getType().accept(this);
		outputString += ") [" + newArrayExpression.getFirstDimension() + "]";
		int dim = newArrayExpression.getDimensions();

		// print ([])*
		for (int i = 0; i < dim; i++) {
			outputString += "[]";
		}
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		// TODO: right format!
		outputString += "(new " + newObjectExpression.getIdentifier() + "())";
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// outputString += variableAccessExpression.getIdentifier().getValue();
		// outputString += "_"; // FIXME This should be the real identifier
		// TODO: right format!
		outputString += "";
		variableAccessExpression.getExpression().accept(this);
		outputString += "." + variableAccessExpression.getFieldIdentifier().getValue() + ")";
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// TODO: right format!
		outputString += "(";
		arrayAccessExpression.getArrayExpression().accept(this);
		outputString += "[";
		arrayAccessExpression.getIndexExpression().accept(this);
		outputString += "])";
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		outputString += "!";
		logicalNotExpression.getOperand().accept(this);
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		outputString += "-";
		negateExpression.getOperand().accept(this);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StringLiteral literal) {
		outputString += literal.getSymbol().getValue();
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		outputString += "this";
	}

	@Override
	public void visit(NullExpression nullExpression) {
		outputString += "null";
	}

	@Override
	public void visit(Type type) {
		String typeString;
		switch (type.getBasicType()) {
		case INT:
			typeString = "int";
			break;
		case VOID:
			typeString = "void";
			break;
		case BOOLEAN:
			typeString = "boolean";
			break;
		case CLASS:
			typeString = "class";
			break;
		default:
			typeString = "array";
			break;
		}
		outputString += typeString;
	}

	@Override
	public void visit(Block block) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ClassMember classMember) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IfStatement ifStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhileStatement whileStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		// TODO Auto-generated method stub

	}

}
