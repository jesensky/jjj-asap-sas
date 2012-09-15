/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or GITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2012 James Jesensky
 */

package jjj.asap.sas.weka;

import weka.core.tokenizers.NGramTokenizer;

/**
 * Like an NGramTokenizer, but on characters instead of words.
 */
public class QGramTokenizer extends NGramTokenizer implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see weka.core.tokenizers.NGramTokenizer#tokenize(java.lang.String)
	 */
	@Override
	public void tokenize(String s) {
		
		final String t = s.replaceAll(" ", "_").toLowerCase();
		StringBuilder buffer = new StringBuilder();
		if(t.length() == 0) {
			buffer.append("x");
		} else {
			buffer.append(t.charAt(0));
		}
		for(int i=1;i<t.length();i++) {
			buffer.append(" ");
			buffer.append(t.charAt(i));
		}
		// 
		super.tokenize(buffer.toString());
	}
	
}
