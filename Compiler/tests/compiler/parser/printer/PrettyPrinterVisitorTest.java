package compiler.parser.printer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
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
import compiler.lexer.Position;

public class PrettyPrinterVisitorTest {

	private PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
	private Position position = new Position(0, 0);
	private Type type = new Type(position, BasicType.INT);
	private VariableAccessExpression variable = new VariableAccessExpression(position, null, new Symbol("_"));

	@Test
	public void testVisitAdditionExpression() {
		visitor.visit(new AdditionExpression(position, variable, variable));
		assertEquals("_ + _", visitor.getOutputString());
	}

	@Test
	public void testVisitAssignmentExpression() {
		visitor.visit(new AdditionExpression(position, variable, variable));
		assertEquals("_ + _", visitor.getOutputString());
	}

	@Test
	public void testVisitDivisionExpression() {
		visitor.visit(new DivisionExpression(position, variable, variable));
		assertEquals("_ / _", visitor.getOutputString());
	}

	@Test
	public void testVisitEqualityExpression() {
		visitor.visit(new EqualityExpression(position, variable, variable));
		assertEquals("_ == _", visitor.getOutputString());
	}

	@Test
	public void testVisitGreaterThanEqualExpression() {
		visitor.visit(new GreaterThanEqualExpression(position, variable, variable));
		assertEquals("_ >= _", visitor.getOutputString());
	}

	@Test
	public void testVisitGreaterThanExpression() {
		visitor.visit(new GreaterThanExpression(position, variable, variable));
		assertEquals("_ > _", visitor.getOutputString());
	}

	@Test
	public void testVisitLessThanEqualExpression() {
		visitor.visit(new LessThanEqualExpression(position, variable, variable));
		assertEquals("_ <= _", visitor.getOutputString());
	}

	@Test
	public void testVisitLessThanExpression() {
		visitor.visit(new LessThanExpression(position, variable, variable));
		assertEquals("_ < _", visitor.getOutputString());
	}

	@Test
	public void testVisitLogicalAndExpression() {
		visitor.visit(new LogicalAndExpression(position, variable, variable));
		assertEquals("_ && _", visitor.getOutputString());
	}

	@Test
	public void testVisitLogicalOrExpression() {
		visitor.visit(new LogicalOrExpression(position, variable, variable));
		assertEquals("_ || _", visitor.getOutputString());
	}

	@Test
	public void testVisitModuloExpression() {
		visitor.visit(new ModuloExpression(position, variable, variable));
		assertEquals("_ % _", visitor.getOutputString());
	}

	@Test
	public void testVisitMuliplicationExpression() {
		visitor.visit(new MuliplicationExpression(position, variable, variable));
		assertEquals("_ * _", visitor.getOutputString());
	}

	@Test
	public void testVisitNonEqualityExpression() {
		visitor.visit(new NonEqualityExpression(position, variable, variable));
		assertEquals("_ != _", visitor.getOutputString());
	}

	@Test
	public void testVisitSubtractionExpression() {
		visitor.visit(new SubtractionExpression(position, variable, variable));
		assertEquals("_ - _", visitor.getOutputString());
	}

	@Test
	public void testVisitBooleanConstantTrueExpression() {
		visitor.visit(new BooleanConstantExpression(position, true));
		assertEquals("true", visitor.getOutputString());
	}

	@Test
	public void testVisitBooleanConstantFalseExpression() {
		visitor.visit(new BooleanConstantExpression(position, false));
		assertEquals("false", visitor.getOutputString());
	}

	@Test
	public void testVisitIntegerConstantExpression() {
		visitor.visit(new IntegerConstantExpression(position, "42"));
		assertEquals("42", visitor.getOutputString());
	}

	@Test
	public void testVisitMethodInvocationExpression1() {
		Expression[] parameters2 = { variable, variable };
		visitor.visit(new MethodInvocationExpression(position, variable, new Symbol("-"), parameters2));
		assertEquals("_.-(_, _)", visitor.getOutputString());
	}

	@Test
	public void testVisitMethodInvocationExpression2() {
		Expression[] parameters1 = { variable };
		visitor.visit(new MethodInvocationExpression(position, null, new Symbol("-"), parameters1));
		assertEquals("-(_)", visitor.getOutputString());
	}

	@Test
	public void testVisitNewArrayExpression() {
		visitor.visit(new ArrayAccessExpression(position, variable, variable));
		assertEquals("_[_]", visitor.getOutputString());
	}

	@Test
	public void testVisitNewObjectExpression() {
		visitor.visit(new NewObjectExpression(position, new Symbol("_")));
		assertEquals("new _()", visitor.getOutputString());
	}

	@Test
	public void testVisitVariableAccessExpression() {
		visitor.visit(new VariableAccessExpression(position, variable, new Symbol("-")));
		assertEquals("_.-", visitor.getOutputString());
	}

	@Test
	public void testVisitArrayAccessExpression() {
		visitor.visit(new ArrayAccessExpression(position, variable, variable));
		assertEquals("_[_]", visitor.getOutputString());
	}

	@Test
	public void testVisitLogicalNotExpression() {
		visitor.visit(new LogicalNotExpression(position, variable));
		assertEquals("!_", visitor.getOutputString());
	}

	@Test
	public void testVisitNegateExpression() {
		visitor.visit(new NegateExpression(position, variable));
		assertEquals("-_", visitor.getOutputString());
	}

	@Test
	public void testVisitReturnStatement() {
		visitor.visit(new ReturnStatement(position, variable));
		assertEquals("return _", visitor.getOutputString());
	}

	@Test
	public void testVisitThisExpression() {
		visitor.visit(new ThisExpression(position));
		assertEquals("this", visitor.getOutputString());
	}

	@Test
	public void testVisitNullExpression() {
		visitor.visit(new NullExpression(position));
		assertEquals("null", visitor.getOutputString());
	}

	@Test
	public void testVisitType() {
		visitor.visit(new Type(position, BasicType.INT));
		assertEquals("int", visitor.getOutputString());
	}

	@Test
	public void testVisitBlock() {
		Block block = new Block(position);
		block.addStatement(variable);
		block.addStatement(variable);
		block.addStatement(variable);
		visitor.visit(block);
		assertEquals("{\n\t_;\n\t_;\n\t_;\n}", visitor.getOutputString());
	}

	@Test
	public void testVisitClassDeclaration() {
		visitor.visit(new ClassDeclaration(position, new Symbol("_")));
		assertEquals("class _ { }\n", visitor.getOutputString());
	}

	@Test
	public void testVisitIfStatement1() {
		visitor.visit(new IfStatement(position, variable, new Block(variable)));
		assertEquals("if (_) {\n\t_;\n}", visitor.getOutputString());

	}

	@Test
	public void testVisitIfStatement2() {
		visitor.visit(new IfStatement(position, variable, new Block(variable), variable));
		assertEquals("if (_) {\n\t_;\n} else\n\t_;", visitor.getOutputString());

	}

	@Test
	public void testVisitIfStatement3() {
		visitor.visit(new IfStatement(position, variable, variable, variable));
		assertEquals("if (_)\n\t_;\nelse\n\t_;", visitor.getOutputString());
	}

	@Test
	public void testVisitWhileStatement1() {
		visitor.visit(new WhileStatement(position, variable, variable));
		assertEquals("while (_)\n\t_;", visitor.getOutputString());

	}

	@Test
	public void testVisitWhileStatement2() {
		visitor.visit(new WhileStatement(position, variable, new Block(variable)));
		assertEquals("while (_) {\n\t_;\n}", visitor.getOutputString());
	}

	@Test
	public void testVisitLocalVariableDeclaration1() {
		visitor.visit(new LocalVariableDeclaration(position, type, new Symbol("-")));
		assertEquals("int -", visitor.getOutputString());
	}

	@Test
	public void testVisitLocalVariableDeclaration2() {
		visitor.visit(new LocalVariableDeclaration(position, type, new Symbol("-"), variable));
		assertEquals("int - = _", visitor.getOutputString());
	}

	@Test
	public void testVisitParameterDefinition() {
		visitor.visit(new ParameterDefinition(position, type, new Symbol("_")));
		assertEquals("int _", visitor.getOutputString());
	}

	@Test
	public void testVisitProgram() {
		Program program = new Program(position);
		program.addClassDeclaration(new ClassDeclaration(position, new Symbol("_")));
		visitor.visit(program);
		assertEquals("class _ { }\n", visitor.getOutputString());
	}

	@Test
	public void testVisitMethodDeclaration1() {
		MethodDeclaration methodDeclaration = new MethodDeclaration(position, new Symbol("_"), type);
		methodDeclaration.setBlock(new Block((Position) null));

		visitor.visit(methodDeclaration);
		assertEquals("public int _() { }\n", visitor.getOutputString());
	}

	@Test
	public void testVisitMethodDeclaration2() {
		MethodDeclaration methodDeclaration = new MethodDeclaration(position, new Symbol("_"), type);
		Block block = new Block(position);
		methodDeclaration.setBlock(block);
		visitor.visit(methodDeclaration);
		assertEquals("public int _() { }\n", visitor.getOutputString());
	}

	@Test
	public void testVisitMethodDeclaration3() {
		MethodDeclaration methodDeclaration = new MethodDeclaration(position, new Symbol("_"), type);
		Block block = new Block(position);
		methodDeclaration.setBlock(block);
		block.addStatement(variable);
		visitor.visit(methodDeclaration);
		assertEquals("public int _() {\n\t_;\n}\n", visitor.getOutputString());
	}

	@Test
	public void testVisitFieldDeclaration() {
		visitor.visit(new FieldDeclaration(position, type, new Symbol("_")));
		assertEquals("public int _;\n", visitor.getOutputString());
	}
}
