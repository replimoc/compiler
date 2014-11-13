package compiler.parser;

public class ParsingFailedException extends Exception {

	private static final long serialVersionUID = -1402357370805753446L;
	private final int detectedErrors;

	public ParsingFailedException(int detectedErrors) {
		this.detectedErrors = detectedErrors;
	}

	@Override
	public String toString() {
		return getDetectedErrors() + " Errors have been detected during parsing.";
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public int getDetectedErrors() {
		return detectedErrors;
	}
}
