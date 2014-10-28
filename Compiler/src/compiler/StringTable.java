package compiler;

import java.util.HashMap;

import compiler.lexer.TokenType;

/**
 * Implementation of StringTable: Data Structure storing identifiers for efficient lookup.
 * The stored object is called Symbol.
 * 
 * @author Aleksej Frank
 *
 */
public class StringTable {
	
	/**
	 * Entry in StringTable
	 */
	public class Entry {
		private final Symbol symbol;
		private final TokenType type;
		
		public Entry(String string, TokenType type) {
			this.symbol = new Symbol(string);
			this.type = type;
		}

		public Symbol getSymbol() {
			return symbol;
		}

		public TokenType getType() {
			return type;
		}
	}

	private HashMap<String, Entry> entries = new HashMap<String, Entry>();
	
	/**
	 * Returns corresponding symbol for given string.
	 * @param string String to store.
	 * @return Symbol representing given string.
	 */
	public Entry insert(String string, TokenType type) {
		Entry entry = entries.get(string);
		if (entry == null) {
			entry = new Entry(string, type);
			entries.put(string, entry);
		}
		return entry;
	}
	
	//insert_token_type
}
