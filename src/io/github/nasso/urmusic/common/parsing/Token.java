package io.github.nasso.urmusic.common.parsing;

public class Token<T> {
	private T type;
	private String value;
	private int line, col;
	
	public Token(T type, String value, int line, int col) {
		this.type = type;
		this.value = value;
		this.line = line;
		this.col = col;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public int getColumn() {
		return this.col;
	}
	
	public T getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return "Token[" + type + "; " + value + "]@[" + line + ":" + col + "]";
	}
}
