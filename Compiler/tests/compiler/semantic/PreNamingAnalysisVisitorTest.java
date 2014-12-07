package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.ClassDeclaration;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.lexer.TokenType;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;

public class PreNamingAnalysisVisitorTest {

	private final PreNamingAnalysisVisitor visitor = new PreNamingAnalysisVisitor();
	private final StringTable stringTable = new StringTable();

	private final MethodDefinition[] testMethods = new MethodDefinition[] {
			m("method1", t(BasicType.INT), d("param1", t(BasicType.INT))),
			m("method2", t(BasicType.VOID), d("param1", t(BasicType.INT))),
			m("method3", t(BasicType.VOID), d("param1", t(BasicType.INT)), d("param1", t(BasicType.BOOLEAN)), d("param1", t(BasicType.INT, 3))),
			m("method4", t(BasicType.VOID), d("param1", t(BasicType.INT)))
	};

	private final Definition[] testFields = new Definition[] {
			d("field1", t(BasicType.INT)),
			d("field2", t(BasicType.BOOLEAN)),
			d("field3", t(BasicType.VOID)),
	};

	@Test
	public void testEmptyProgram() {
		Program program = new Program(null);
		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(0, classes.size());
		assertEquals(1, visitor.getExceptions().size());
	}

	@Test
	public void testSingleClass() {
		Program program = new Program(null);
		Symbol class1 = s("class1");
		program.addClassDeclaration(new ClassDeclaration(null, class1));

		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(1, classes.size());
		ClassScope class1Scope = classes.get(class1);
		assertNotNull(class1Scope);
		assertEquals(0, class1Scope.getNumberOfFields());
		assertEquals(0, class1Scope.getNumberOfMethods());
		assertEquals(1, visitor.getExceptions().size()); // no main
	}

	@Test
	public void testDoubleClassDefinition() {
		Program program = new Program(null);
		Symbol class1 = s("class1");
		program.addClassDeclaration(new ClassDeclaration(null, class1));
		program.addClassDeclaration(new ClassDeclaration(null, class1));

		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(1, classes.size());
		ClassScope class1Scope = classes.get(class1);
		assertNotNull(class1Scope);
		assertEquals(0, class1Scope.getNumberOfFields());
		assertEquals(0, class1Scope.getNumberOfMethods());

		// no main found and double class
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testSingleClassSingleMethod() {
		ClassScope scope1 = scope(f(), m(1));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertEquals(expectedScopes, visitor.getClassScopes());
		assertEquals(1, visitor.getExceptions().size()); // no main
	}

	@Test
	public void testClassTwoFieldsThreeMethods() {
		ClassScope scope1 = scope(f(1, 2), m(1, 2, 3));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1), c("class2", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertEquals(expectedScopes, visitor.getClassScopes());
		assertEquals(1, visitor.getExceptions().size());
	}

	@Test
	public void testDoubleFieldDefinition() {
		ClassScope scope1 = scope(f(1, 2, 0, 1), m());
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertEquals(expectedScopes, visitor.getClassScopes());
		assertEquals(1, visitor.getExceptions().size());
	}

	@Test
	public void testValidMain() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.VOID), d("args", t(ct("String"), 1)))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertNotNull(visitor.getClassScopes().get(s("class1")));
		assertEquals(1, visitor.getClassScopes().get(s("class1")).getNumberOfMethods());
		assertTrue(visitor.hasMain());
		assertEquals(0, visitor.getExceptions().size());
	}

	@Test
	public void testMainInvalidReturnType() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.INT), d("args", t(ct("String"), 1)))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainNoParameter() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.VOID))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainInvalidParameterArrayType() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.VOID), d("args", t(ct("Bla"), 1)))));
		ClassScope scope2 = scope(f(), m());
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1), c("Bla", scope2));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainInvalidParameterNoArrayType() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.VOID), d("args", t(BasicType.INT)))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainNoArrayParameterType() {
		ClassScope scope1 = scope(f(), asArray(m("main", t(BasicType.INT), d("args", ct("String")))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	private static Program createAst(HashMap<Symbol, ClassScope> scopes) {
		Program program = new Program(null);

		for (Entry<Symbol, ClassScope> curr : scopes.entrySet()) {
			Symbol name = curr.getKey();
			ClassScope scope = curr.getValue();

			program.addClassDeclaration(createClassDeclaration(name, scope.getFieldDefinitions(), scope.getMethodDefinitions()));
		}

		return program;
	}

	@SafeVarargs
	private static <T> T[] asArray(T... t) {
		return t;
	}

	private static ClassDeclaration createClassDeclaration(Symbol name, Definition[] fields, MethodDefinition[] methods) {
		ClassDeclaration classDeclaration = new ClassDeclaration(null, name);
		for (Definition field : fields) {
			classDeclaration.addClassMember(new FieldDeclaration(null, field.getType(), field.getSymbol()));
		}
		for (MethodDefinition method : methods) {
			MethodDeclaration methodDeclaration;
			if ("main".equals(method.getSymbol().getValue())) { // hack to test static main
				methodDeclaration = new StaticMethodDeclaration(null, method.getSymbol(), method.getType());
			} else {
				methodDeclaration = new MethodDeclaration(null, method.getSymbol(), method.getType());
			}

			for (Definition param : method.getParameters()) {
				methodDeclaration.addParameter(new ParameterDefinition(null, param.getType(), param.getSymbol()));
			}
			classDeclaration.addClassMember(methodDeclaration);
		}
		return classDeclaration;
	}

	@SafeVarargs
	private static HashMap<Symbol, ClassScope> scopes(Entry<Symbol, ClassScope>... scopes) {
		HashMap<Symbol, ClassScope> scopesMap = new HashMap<>();

		for (Entry<Symbol, ClassScope> curr : scopes) {
			scopesMap.put(curr.getKey(), curr.getValue());
		}
		return scopesMap;
	}

	private static ClassScope scope(Definition[] fields, MethodDefinition[] methods) {
		HashMap<Symbol, Definition> fieldsMap = new HashMap<>();
		HashMap<Symbol, MethodDefinition> methodsMap = new HashMap<>();

		for (Definition curr : fields) {
			fieldsMap.put(curr.getSymbol(), curr);
		}
		for (MethodDefinition curr : methods) {
			methodsMap.put(curr.getSymbol(), curr);
		}

		return new ClassScope(fieldsMap, methodsMap);
	}

	private Entry<Symbol, ClassScope> c(String string, ClassScope scope) {
		return new SimpleEntry<>(s(string), scope);
	}

	private MethodDefinition[] m(int... indexes) {
		MethodDefinition[] definitions = new MethodDefinition[indexes.length];

		for (int i = 0; i < indexes.length; i++) {
			definitions[i] = testMethods[indexes[i]];
		}

		return definitions;
	}

	private Definition[] f(int... indexes) {
		Definition[] definitions = new Definition[indexes.length];

		for (int i = 0; i < indexes.length; i++) {
			definitions[i] = testFields[indexes[i]];
		}

		return definitions;
	}

	private Symbol s(String string) {
		return stringTable.insert(string, TokenType.IDENTIFIER).getSymbol();
	}

	private static Type t(BasicType basicType) {
		return new Type(null, basicType);
	}

	private static Type t(BasicType basicType, int dimensions) {
		return t(t(basicType), dimensions);
	}

	private static Type t(Type type, int dimensions) {
		for (int i = 0; i < dimensions; i++) {
			type = new ArrayType(null, type);
		}
		return type;
	}

	private MethodDefinition m(String name, Type returnType, Definition... parameters) {
		return new MethodDefinition(s(name), returnType, parameters, null);
	}

	private Definition d(String name, Type type) {
		return new Definition(s(name), type, null);
	}

	private Type ct(String string) {
		return new ClassType(null, s(string));
	}
}
