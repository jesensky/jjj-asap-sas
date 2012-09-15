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

package jjj.asap.sas.util;

/**
 * Methods to help working with strings
 */
public class StringUtils {

	/**
	 * Some NLP routines can't handle empty strings, so use a short
	 * string instead.
	 * @return "x" if the parameter is null, empty, or blank
	 */
	public static String safeForNLP(final String text) {
		if(text == null || text.isEmpty() || text.trim().isEmpty()) {
			return "x";
		} else {
			return text;
		}
	}
	
	
	/**
	 * Always returns an array of size length possibly adding empty strings
	 */
	public static String[] safeSplit(final String text, final String regex, final int length) {
		
		String[] safe = new String[length];
		for(int i=0;i<safe.length;i++) {
			safe[i] = "";
		}
		
		if(text != null) {
			String[] parts = text.split(regex,length);
			for(int i=0;i<parts.length;i++) {
				if(parts[i] != null && i<safe.length) {
					safe[i] = parts[i].trim();
				}
			}
			
		}
		
		return safe;
	}
	
}
