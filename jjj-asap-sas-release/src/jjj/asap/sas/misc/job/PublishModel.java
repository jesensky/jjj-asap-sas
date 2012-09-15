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

package jjj.asap.sas.misc.job;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.weka.Model;

public class PublishModel extends Job {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Job job = new PublishModel(args[0]);
		job.start();
	}
	
	private String filename;
	
	public PublishModel(String filename) {
		super();
		this.filename = filename;
	}


	@Override
	protected void run() throws Exception {

		final String input = "work/models3/" + filename;
		final String output = "publish/" + filename;
		
		if(IOUtils.exists(output)) {
			Job.log("PUBLISH",output + " already exists - nothing to do.");
			return;
		}
		
		// the input file is probably already in the correct format, but technically
		// the predictions in the final file need to be in the same order as the 
		// test set.
		
		Map<Double,Double> unordered = Model.loadPredictions(input);
		Map<Double,Double> ordered = new LinkedHashMap<Double,Double>();
		
		Iterator<String> it = new FileIterator("data/test.tsv");
		it.next(); // skip header row
		while(it.hasNext()) {
			String line = it.next();
			int pos = line.indexOf('\t');
			double id = Double.valueOf(line.substring(0,pos));
			ordered.put(id, unordered.get(id));
		}
		
		// finally save it
		Model.savePredictions(output, ordered);
		Job.log("PUBLISH","New submission now available at " + output);

	}

	
}
