/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.common.parsing;

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
