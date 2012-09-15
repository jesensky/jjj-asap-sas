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
 * A very simple timer class--just returns elapsed seconds since construction.
 */
public class Timer {

	private long begin;
	
	public Timer() {
		this.begin = System.currentTimeMillis();
	}
	
	/**
	 * @return approximate seconds since Timer was created.
	 */
	public int getElapsedSeconds() {
		return (int)((System.currentTimeMillis()-begin)/1000.0);
	}
	
}
