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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods dealing with IO
 */
public class IOUtils {

	public static String getName(final String path) {
		return new File(path).getName();
	}
	
	/**
	 * Checks if a directory already exists. Creates it if it does (but still
	 * returns false in that case)
	 */
	public static boolean exists(final String name) {
		File file = new File(name);
		return file.exists();
	}
	
	/**
	 * @return a list of file names from the work directory that match the regex
	 */
	public static List<String> getFileNames(final String path,final String regex) {
		
		List<String> names = new ArrayList<String>();
		
		File dir = new File(path);
		String[] files = dir.list();
		for(String file : files) {
			if(file.matches(regex)) {
				names.add(file);
			}
		}
			
		return names;
	}

	public static void touch(String answers) {
		File file = new File(answers);
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new RuntimeException(answers,e);
		}
	}
	
	public static void createAsNeeded(String dirName) {
		File dir = new File(dirName);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}
	
}
