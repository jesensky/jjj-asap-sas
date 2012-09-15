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
 * Simple class that prints progress to console in multiples of 1%.
 */
public class Progress {

	private int max;
	private int current;
	private String ofWhat;
	private int lastPercent;
	
	public Progress(int max,String ofWhat) {
		this.max = max;
		this.ofWhat = ofWhat;
		this.lastPercent = -1;
		setProgress(0);
	}
	
	public void tick() {
		setProgress(current+1);
	}
	
	public void setProgress(int current) {
		this.current = current;
		int currentPercent = (int)((current*100.0)/max);
		if(currentPercent != lastPercent) {
			lastPercent = currentPercent;
			Job.log("PROGRESS",ofWhat + " " + currentPercent + "%");
		}
	}
	
	public void done() {
		setProgress(max);
	}
	
}
