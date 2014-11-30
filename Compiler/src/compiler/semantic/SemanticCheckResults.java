package compiler.semantic;

import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
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

}
