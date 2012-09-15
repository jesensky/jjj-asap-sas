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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Iterates over a file. Each row in the file is one iteration.
 */
public class FileIterator implements Iterator<String> {

	private static final int BUFFER_SIZE = 8192;
	
	private FileReader file;
	private BufferedReader buffer;
	private String next;

	public FileIterator(File fileObject) {
		try {
			this.file = new FileReader(fileObject);
			this.buffer = new BufferedReader(this.file,BUFFER_SIZE);
			next = buffer.readLine();
		} catch (IOException e) {
			throw new RuntimeException(fileObject.getPath(),e);
		}
	}
	
	public FileIterator(String filename) {
		this(new File(filename));
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		String current = next;
		try {
			next = buffer.readLine();
		} catch (IOException e) {
			throw new RuntimeException(current,e);
		}
		return current;
	}

	@Override
	public void remove() {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		buffer.close();
		file.close();
		super.finalize();
	}

	
}
