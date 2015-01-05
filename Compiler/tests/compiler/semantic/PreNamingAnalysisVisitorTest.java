package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.Declaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MainMethodDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.lexer.Position;
import compiler.lexer.TokenType;

public class PreNamingAnalysisVisitorTest {

	private final PreNamingAnalysisVisitor visitor = new PreNamingAnalysisVisitor();
	private final StringTable stringTable = new StringTable();

	private final MethodDeclaration[] testMethods = {
			m("method1", false, t(BasicType.INT), pd("param1", t(BasicType.INT))),
			m("method2", false, t(BasicType.VOID), pd("param1", t(BasicType.INT))),
			m("method3", false, t(BasicType.VOID), pd("param1", t(BasicType.INT)),
					pd("param1", t(BasicType.BOOLEAN)), pd("param1", t(BasicType.INT, 3))),
			m("method4", false, t(BasicType.VOID), pd("param1", t(BasicType.INT)))
	};

	private final FieldDeclaration[] testFields = new FieldDeclaration[] {
			fd("field1", t(BasicType.INT)),
			fd("field2", t(BasicType.BOOLEAN)),
			fd("field3", t(BasicType.VOID)),
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
	public void testDoubleClassDeclaration() {
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
	public void testDoubleFieldDeclaration() {
		ClassScope scope1 = scope(f(1, 2, 0, 1), m());
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertEquals(expectedScopes, visitor.getClassScopes());
		assertEquals(1, visitor.getExceptions().size());
	}

	@Test
	public void testValidMain() {
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.VOID), pd("args", t(ct("String"), 1)))));
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
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.INT), pd("args", t(ct("String"), 1)))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainNoParameter() {
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.VOID))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainInvalidParameterArrayType() {
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.VOID), pd("args", t(ct("Bla"), 1)))));
		ClassScope scope2 = scope(f(), m());
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1), c("Bla", scope2));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainInvalidParameterNoArrayType() {
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.VOID), pd("args", t(BasicType.INT)))));
		HashMap<Symbol, ClassScope> expectedScopes = scopes(c("class1", scope1));

		Program program = createAst(expectedScopes);
		program.accept(visitor);

		assertFalse(visitor.hasMain());
		assertEquals(2, visitor.getExceptions().size());
	}

	@Test
	public void testMainNoArrayParameterType() {
		ClassScope scope1 = scope(f(), asArray(m("main", true, t(BasicType.INT), pd("args", ct("String")))));
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

			program.addClassDeclaration(createClassDeclaration(name, scope.getFieldDeclarations(), scope.getMethodDeclarations()));
		}

		return program;
	}

	@SafeVarargs
	private static <T> T[] asArray(T... t) {
		return t;
	}

	private static ClassDeclaration createClassDeclaration(Symbol name, Declaration[] fields, MethodMemberDeclaration[] methods) {
		ClassDeclaration classDeclaration = new ClassDeclaration(null, name);
		for (Declaration field : fields) {
			classDeclaration.addClassMember(new FieldDeclaration(null, field.getType(), field.getIdentifier()));
		}
		for (MethodMemberDeclaration methodDeclaration : methods) {
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

	private static ClassScope scope(FieldDeclaration[] fields, MethodMemberDeclaration[] methods) {
		HashMap<Symbol, FieldDeclaration> fieldsMap = new HashMap<>();
		HashMap<Symbol, MethodMemberDeclaration> methodsMap = new HashMap<>();

		for (FieldDeclaration curr : fields) {
			fieldsMap.put(curr.getIdentifier(), curr);
		}
		for (MethodMemberDeclaration curr : methods) {
			methodsMap.put(curr.getIdentifier(), curr);
		}

		return new ClassScope(fieldsMap, methodsMap);
	}

	private Entry<Symbol, ClassScope> c(String string, ClassScope scope) {
		return new SimpleEntry<>(s(string), scope);
	}

	private MethodDeclaration[] m(int... indexes) {
		MethodDeclaration[] declarations = new MethodDeclaration[indexes.length];

		for (int i = 0; i < indexes.length; i++) {
			declarations[i] = testMethods[indexes[i]];
		}

		return declarations;
	}

	private FieldDeclaration[] f(int... indexes) {
		FieldDeclaration[] declarations = new FieldDeclaration[indexes.length];

		for (int i = 0; i < indexes.length; i++) {
			declarations[i] = testFields[indexes[i]];
		}

		return declarations;
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

	private MethodDeclaration m(String name, boolean isStatic, Type returnType, ParameterDeclaration... parameters) {
		Block block = new Block((Position) null);
		List<ParameterDeclaration> parameterList = Arrays.asList(parameters);

		if ("main".equals(name) && isStatic) {
			return new MainMethodDeclaration(null, s(name), parameterList, returnType, block);
		} else {
			return new MethodDeclaration(null, isStatic, s(name), parameterList, returnType, block);
		}
	}

	private ParameterDeclaration pd(String name, Type type) {
		return new ParameterDeclaration(type, s(name));
	}

	private FieldDeclaration fd(String name, Type type) {
		return new FieldDeclaration(type, s(name));
	}

	private Type ct(String string) {
		return new ClassType(null, s(string));
	}
}
