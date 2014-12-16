package compiler.semantic;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.CallingConvention;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.semantic.exceptions.SemanticAnalysisException;

public class SemanticCheckResults {

	private final List<SemanticAnalysisException> exceptions;
	private final HashMap<Symbol, ClassScope> classScopes;

	public SemanticCheckResults(List<SemanticAnalysisException> exceptions, HashMap<Symbol, ClassScope> classScopes) {
		this.exceptions = exceptions;
		this.classScopes = classScopes;
	}

	public List<SemanticAnalysisException> getExceptions() {
		return exceptions;
	}

	public HashMap<Symbol, ClassScope> getClassScopes() {
		return classScopes;
	}

	public boolean hasErrors() {
		return !exceptions.isEmpty();
	}

	public int getNumberOfExceptions() {
		return exceptions.size();
	}

	public HashMap<String, CallingConvention> getCallingConventions() {
		HashMap<String, CallingConvention> callingConventions = new HashMap<String, CallingConvention>();
		for (Entry<Symbol, ClassScope> entry : classScopes.entrySet()) {
			for (MethodMemberDeclaration methodDeclaration : entry.getValue().getMethodDeclarations()) {
				if (methodDeclaration instanceof MethodMemberDeclaration) {
					callingConventions.put(methodDeclaration.getAssemblerName(), methodDeclaration.getCallingConvention());
				}
			}
		}
		return callingConventions;
	}
}
