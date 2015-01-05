package compiler.utils;

public class ExecutionFailedException extends Exception {
	private static final long serialVersionUID = 8972349940857890542L;
	private final int statusCode;

	public ExecutionFailedException(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

}
