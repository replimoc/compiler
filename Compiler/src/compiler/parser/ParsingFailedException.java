package compiler.parser;

import java.util.List;

public class ParsingFailedException extends Exception {

	private static final long serialVersionUID = -1402357370805753446L;
	private List<ParserException> detectedErrors;

	public ParsingFailedException(List<ParserException> detectedErrors) {
		this.detectedErrors = detectedErrors;
	}

	@Override
	public String toString() {
		return getDetectedErrors().size() + " Errors have been detected during parsing.";
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public List<ParserException> getDetectedErrors() {
		return detectedErrors;
	}

	public void printParserExceptions() {
		System.err.println(toString());
		for (ParserException exception : detectedErrors) {
			System.err.println(exception);
		}
	}
}
