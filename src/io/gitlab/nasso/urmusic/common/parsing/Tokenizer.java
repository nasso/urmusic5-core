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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer<T> {
	private static final Pattern LINE_PAT = Pattern.compile("(\r\n)|(\r)|(\n)");
	
	private List<T> tokensTypes = new ArrayList<T>();
	private List<Pattern> tokensRegex = new ArrayList<Pattern>();
	private List<Pattern> ignoreTokens = new ArrayList<Pattern>();
	
	public Tokenizer() {
		
	}
	
	public void addToken(String regex, T type, int flags) {
		this.tokensTypes.add(type);
		this.tokensRegex.add(Pattern.compile("^" + regex, Pattern.UNICODE_CHARACTER_CLASS | flags));
	}
	
	public void ignore(String regex, int flags) {
		this.ignoreTokens.add(Pattern.compile("^" + regex, Pattern.UNICODE_CHARACTER_CLASS | flags));
	}
	
	public void addToken(String regex, T type) {
		this.addToken(regex, type, 0);
	}
	
	public void ignore(String regex) {
		this.ignore(regex, 0);
	}
	
	private Token<T> readToken(StringBuffer buf, int line, int col) {
		for(int i = 0; i < this.ignoreTokens.size(); i++) {
			Matcher m = this.ignoreTokens.get(i).matcher(buf);
			if(m.find()) {
				String str = m.group();
				
				Matcher mat = LINE_PAT.matcher(str);
				while(mat.find()) {
					col = mat.end();
					line++;
				}
				
				col = str.length() - col;
				
				buf.delete(0, m.end());
				i = -1;
			}
		}
		
		if(buf.length() == 0) return null;
		
		for(int i = 0; i < this.tokensTypes.size(); i++) {
			T type = this.tokensTypes.get(i);
			Matcher m = this.tokensRegex.get(i).matcher(buf);
			
			if(m.find()) {
				String val = buf.substring(0, m.end());
				buf.delete(0, m.end());
				return new Token<T>(type, val, line, col);
			}
		}
		
		return null;
	}
	
	public List<Token<T>> tokenize(CharSequence str) {
		List<Token<T>> tokenList = new ArrayList<Token<T>>();
		StringBuffer buf = new StringBuffer(str);
		
		int line = 0, col = 0;
		
		Token<T> tk;
		while(buf.length() != 0) {
			line = 0;
			
			Matcher mat = LINE_PAT.matcher(str);
			while(mat.find() && mat.start() < (str.length() - buf.length())) {
				col = mat.end();
				line++;
			}
			
			col = str.length() - col;
			
			tk = this.readToken(buf, line, col);
			if(tk != null) tokenList.add(tk);
			else if(buf.length() != 0) {
				System.err.println("Unexpected token: '" + buf.toString() + "' @[" + (line + 1) + ":" + (col + 1) + "]");
				break;
			}
		}
		
		return tokenList;
	}
}
