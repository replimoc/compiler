package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.lexer.Position;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.exceptions.NoSuchMemberException;
import compiler.semantic.exceptions.RedefinitionErrorException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.exceptions.UndefinedSymbolException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;
import compiler.utils.TestUtils;

public class DeepCheckingVisitorTest {

	private HashMap<Symbol, ClassScope> classScopes = new HashMap<Symbol, ClassScope>();
	private final DeepCheckingVisitor visitor = new DeepCheckingVisitor(classScopes);
	private final StringTable stringTable = new StringTable();

	@Test
	public void testEmptyProgram() {
		Program program = new Program(null);
		program.accept(visitor);

		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());
	}

	@Test
	public void testVarRedefinitionInMethod() {
		classScopes.put(s("class1"), new ClassScope(new HashMap<Symbol, Definition>(), new HashMap<Symbol, MethodDefinition>()));
		Program program = new Program(null);
		Symbol class1 = s("class1");
		ClassDeclaration classObj = new ClassDeclaration(null, class1);
		MethodDeclaration methodObj = new MethodDeclaration(null, s("method"), t(BasicType.VOID));
		Block blockObj = new Block((Position) null);
		LocalVariableDeclaration locVarA = new LocalVariableDeclaration(null, t(BasicType.CLASS), s("intVar"));
		blockObj.addStatement(locVarA);
		methodObj.setBlock(blockObj);
		classObj.addClassMember(methodObj);
		program.addClassDeclaration(classObj);

		// valid program
		program.accept(visitor);

		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());

		// var redefined
		LocalVariableDeclaration locVarB = new LocalVariableDeclaration(null, t(BasicType.INT), s("intVar"));
		blockObj.addStatement(locVarB);
		program.accept(visitor);

		exceptions = visitor.getExceptions();
		assertEquals(1, exceptions.size());

		RedefinitionErrorException redExp = (RedefinitionErrorException) exceptions.get(0);
		assertEquals(redExp.getIdentifier(), s("intVar"));
		assertEquals(redExp.getRedefinition(), locVarB.getPosition());
	}

	@Test
	public void testVarRedefinitionInMethodParameter() {
		classScopes.put(s("class1"), new ClassScope(new HashMap<Symbol, Definition>(), new HashMap<Symbol, MethodDefinition>()));
		Program program = new Program(null);
		Symbol class1 = s("class1");
		ClassDeclaration classObj = new ClassDeclaration(null, class1);
		MethodDeclaration methodObj = new MethodDeclaration(null, s("method"), t(BasicType.VOID));
		ParameterDefinition paramA = new ParameterDefinition(null, t(BasicType.INT), s("paramA"));
		methodObj.addParameter(paramA);
		classObj.addClassMember(methodObj);
		program.addClassDeclaration(classObj);

		program.accept(visitor);

		// valid program
		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());

		// param redefinition
		ParameterDefinition paramB = new ParameterDefinition(null, t(BasicType.INT), s("paramA"));
		methodObj.addParameter(paramB);
		program.accept(visitor);

		exceptions = visitor.getExceptions();
		assertEquals(1, exceptions.size());

		RedefinitionErrorException redExp = (RedefinitionErrorException) exceptions.get(0);
		assertEquals(s("paramA"), redExp.getIdentifier());
		assertEquals(paramB.getPosition(), redExp.getRedefinition());
	}

	@Test
	public void testVarUndefinedInMethod() {
		classScopes.put(s("class1"), new ClassScope(new HashMap<Symbol, Definition>(), new HashMap<Symbol, MethodDefinition>()));
		Program program = new Program(null);
		Symbol class1 = s("class1");
		ClassDeclaration classObj = new ClassDeclaration(null, class1);
		MethodDeclaration methodObj = new MethodDeclaration(null, s("method"), t(BasicType.VOID));
		Block blockObj = new Block((Position) null);
		VariableAccessExpression varAccess = new VariableAccessExpression(null, null, s("undefVar"));
		blockObj.addStatement(varAccess);
		methodObj.setBlock(blockObj);
		classObj.addClassMember(methodObj);
		program.addClassDeclaration(classObj);

		program.accept(visitor);

		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(1, exceptions.size());
		UndefinedSymbolException redExp = (UndefinedSymbolException) exceptions.get(0);
		assertEquals(redExp.getIdentifier(), s("undefVar"));
		assertEquals(redExp.getPosition(), varAccess.getPosition());
		exceptions.clear();

		// now add the var
		ParameterDefinition paramA = new ParameterDefinition(null, t(BasicType.INT), s("undefVar"));
		methodObj.addParameter(paramA);
		program.accept(visitor);

		exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());
	}

	@Test
	public void testNoSuchMemberCase() {
		classScopes.put(s("class1"), new ClassScope(new HashMap<Symbol, Definition>(), new HashMap<Symbol, MethodDefinition>()));

		Program program = new Program(null);
		Symbol class1 = s("class1");
		ClassDeclaration classObj = new ClassDeclaration(null, class1);
		MethodDeclaration methodObj = new MethodDeclaration(null, s("method"), t(BasicType.VOID));
		methodObj.setType(t(BasicType.CLASS));
		Block blockObj = new Block((Position) null);
		VariableAccessExpression leftExpr = new VariableAccessExpression(null, null, s("myClass1"));
		leftExpr.setType(t(BasicType.CLASS));
		VariableAccessExpression varAccess = new VariableAccessExpression(null, leftExpr, s("memberClass1"));
		blockObj.addStatement(varAccess);
		methodObj.setBlock(blockObj);
		classObj.addClassMember(methodObj);
		program.addClassDeclaration(classObj);

		program.accept(visitor);

		// myClass1 is undefined
		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(2, exceptions.size());
		UndefinedSymbolException undSymb = (UndefinedSymbolException) exceptions.get(0);
		assertNotNull(undSymb);
		exceptions.clear();

		// now myClass1 is defined, but member undefVar is missing
		LocalVariableDeclaration locVarA = new LocalVariableDeclaration(null, new ClassType(null, s("class1")), s("myClass1"));
		blockObj.getStatements().add(0, locVarA);

		program.accept(visitor);

		exceptions = visitor.getExceptions();
		assertEquals(1, exceptions.size());
		NoSuchMemberException nsme = (NoSuchMemberException) exceptions.get(0);
		assertNotNull(nsme);
		exceptions.clear();

		// myClass is defined and undefVar exists
		HashMap<Symbol, Definition> fieldMap = new HashMap<Symbol, Definition>();
		fieldMap.put(s("memberClass1"), new Definition(s("MemberType"), t(BasicType.CLASS)));
		classScopes.put(s("class1"), new ClassScope(fieldMap, new HashMap<Symbol, MethodDefinition>()));
		exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());
	}

	@Test
	public void testFromString() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) {} }");
		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(ClassB param) {} }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param, int param) {} }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { paramA; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { param.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int memberInt; public static void main(String[] args) {} public void function(Class param) { param.memberInt; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { param.memberClass.memberClass.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int param; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int locVarInt; locVarInt.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { Class locVarClass; locVarClass.memberClass; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { method(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.methodA(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method().asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method() {}  public static void main(String[] args) {} public void function(Class param) { param.method().asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) {}  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) {}  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) {}  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) {}  public static void main(String[] args) {} public void function() { this.asdf; this.method(12,12); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) {}  public static void main(String[] args) {} public void function() { this.asdf3; this.method(12,12).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { System.out.println(42); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean args = true; System.out.println(args); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		// assertNotNull((NoSuchMemberException) errors.get(0));

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(false); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(new Class()); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public int m; public void m() { } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class System; public void bla() { System.out.println(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		
		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void bla() { this.bla(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());
		
		/*parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class classA; public void bla(Class classB) { { Class classA; } } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());*/
		
		parser = TestUtils
				.initParser("class Main{public static void main(String[] vargs){vargs[5];}}");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
	}

	private Type t(BasicType type) {
		return new Type(null, type);
	}

	private Symbol s(String string) {
		return stringTable.insert(string, TokenType.IDENTIFIER).getSymbol();
	}
}
