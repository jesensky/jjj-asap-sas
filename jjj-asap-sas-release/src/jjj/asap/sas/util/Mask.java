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

import java.io.Serializable;
import java.util.Arrays;

public class Mask implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean[] mask;
	
	public Mask(int size) {
		this.mask = new boolean[size];
	}

	/**
	 * @return the mask
	 */
	public boolean[] getMask() {
		return mask;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(boolean[] mask) {
		this.mask = mask;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mask);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mask other = (Mask) obj;
		if (!Arrays.equals(mask, other.mask))
			return false;
		return true;
	}
		
	
	
}
