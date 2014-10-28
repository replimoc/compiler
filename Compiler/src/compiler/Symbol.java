package compiler;

/**
 * Symbol represents a hashed String (see StringTable).
 * 
 * @author Aleksej Frank
 *
 */
public class Symbol {
	private String value;
	// definition
	
	public Symbol(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
