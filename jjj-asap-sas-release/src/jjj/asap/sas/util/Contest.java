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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Various helper constants and methods related to the contest format.
 */
public class Contest {

	public static final List<String> COLORS = 
			Arrays.asList("x","black","dark gray","light gray","white");

	public static final List<String> RUBRIC3 = 
			Arrays.asList("0","1","2");

	public static final List<String> RUBRIC4 = 
			Arrays.asList("0","1","2","3");

	public static List<String> getRubrics(final int essaySet) {
		if(essaySet == 1) return RUBRIC4;
		if(essaySet == 2) return RUBRIC4;
		if(essaySet == 5) return RUBRIC4;
		if(essaySet == 6) return RUBRIC4;
		return RUBRIC3;
	}

	public static boolean isMultiChoice(int essaySet) {
		return essaySet == 10;
	}


	private static Map<Double,Double> goldStandard[] = null;

	private static Map<Double,String> silverStandard[] = null;

	/**
	 * Returns the ids and lables for score1 from the training data.
	 * @param essaySet either 1-10 or 0 for all essays
	 */
	public static synchronized Map<Double, Double> getGoldStandard(int essaySet) {

		if(goldStandard == null) {
			goldStandard = new Map[11];
			Map<Double,Double> truth[] = new Map[11];
			for(int i=0;i<11;i++) {
				truth[i] = new HashMap<Double,Double>();
			}
			// read file
			Iterator<String> it = new FileIterator("data/train.tsv");
			it.next(); // skip header
			while(it.hasNext()) {
				final String record = it.next();
				// parse
				final String[] fields = StringUtils.safeSplit(record,"\t",5);
				final double id = Double.valueOf(fields[0]);
				final int k = Integer.valueOf(fields[1]);
				final double score = Double.valueOf(fields[2]); // score1
				// update
				truth[0].put(id,score);
				truth[k].put(id,score);
			}
			// save
			for(int i=0;i<11;i++) {
				goldStandard[i] = Collections.unmodifiableMap(truth[i]);
			}
		}
		return goldStandard[essaySet];
	}


	/**
	 * Returns the ids and lables for score2 from the training data.
	 * @param essaySet either 1-10 or 0 for all essays
	 */
	public static synchronized Map<Double, String> getSilverStandard(int essaySet) {

		if(silverStandard == null) {
			silverStandard = new Map[11];
			Map<Double,String> truth[] = new Map[11];
			for(int i=0;i<11;i++) {
				truth[i] = new HashMap<Double,String>();
			}
			// read file
			Iterator<String> it = new FileIterator("data/train.tsv");
			it.next(); // skip header
			while(it.hasNext()) {
				final String record = it.next();
				// parse
				final String[] fields = StringUtils.safeSplit(record,"\t",5);
				final double id = Double.valueOf(fields[0]);
				final int k = Integer.valueOf(fields[1]);
				final String score = fields[3]; // score2
				// update
				truth[0].put(id,score);
				truth[k].put(id,score);
			}
			// save
			for(int i=0;i<11;i++) {
				silverStandard[i] = Collections.unmodifiableMap(truth[i]);
			}
		}
		return silverStandard[essaySet];
	}
	
	/**
	 * @return an array of rubrics such that P(y_k) > P(y_k+1)
	 */
	public static int[] getPriorRank(int essaySet) {

		switch(essaySet) {

		case 1: return new int[]{2,1,0,3};
		case 2: return new int[]{2,1,3,0};
		case 3: return new int[]{1,0,2};
		case 4: return new int[]{1,0,2};
		case 5: return new int[]{0,1,2,3};
		case 6: return new int[]{0,1,2,3};
		case 7: return new int[]{0,1,2};
		case 8: return new int[]{2,0,1};
		case 9: return new int[]{1,2,0};
		case 10: return new int[]{1,2,0};

		}
		throw new IllegalArgumentException("bad essay set: "+essaySet);
	}
	
	public static int getEssaySet(final String name) {
		if(name != null) {
			if(name.startsWith("1-")) return 1;
			if(name.startsWith("2-")) return 2;
			if(name.startsWith("3-")) return 3;
			if(name.startsWith("4-")) return 4;
			if(name.startsWith("5-")) return 5;
			if(name.startsWith("6-")) return 6;
			if(name.startsWith("7-")) return 7;
			if(name.startsWith("8-")) return 8;
			if(name.startsWith("9-")) return 9;
			if(name.startsWith("10-")) return 10;
		}
		
		throw new IllegalArgumentException(name);
	}
	
	public static Map<Double,Double> getSilverStandardAsNumeric(int essaySet) {
		Map<Double,String> raw = getSilverStandard(essaySet);
		Map<Double,Double> silver = new HashMap<Double,Double>();
		for(Map.Entry<Double, String> r : raw.entrySet()) {
			silver.put(r.getKey(), Double.valueOf(r.getValue()));
		}
		return silver;
	}
	
	
}
