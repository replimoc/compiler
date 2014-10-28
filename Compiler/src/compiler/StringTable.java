package compiler;

import java.util.HashMap;

/**
 * Implementation of StringTable: Data Structure storing identifiers for efficient lookup.
 * The stored object is called Symbol.
 * 
 * @author Aleksej Frank
 *
 */
public class StringTable {

	private HashMap<String, Symbol> entries = new HashMap<String, Symbol>();
	
	/**
	 * Returns corresponding symbol for given string.
	 * @param string String to store.
	 * @return Symbol representing given string.
	 */
	public Symbol insert(String string) {
		Symbol symbol = entries.get(string);
		if (symbol == null) {
			symbol = new Symbol(string);
			entries.put(string, symbol);
		}
		return symbol;
	}
}
