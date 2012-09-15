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

import java.util.Iterator;

public class ForEach<T> implements Iterator<T> {

	private T[] t;
	private int k;

	public ForEach(T...t) {
		this.t = t;
		this.k = 0;
	}

	@Override
	public boolean hasNext() {
		return k<t.length;
	}

	@Override
	public T next() {
		return t[k++];
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
