package compiler.semantic.exceptions;

public class NoMainFoundException extends Exception {
	private static final long serialVersionUID = 709713290711218056L;

	public NoMainFoundException() {
		super("No 'public static void main(String[] args)' method has been found.");
	}

	@Override
	public String toString() {
		return super.getMessage();
	}
}
