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

package jjj.asap.sas.datasets.job;

import java.util.Arrays;
import java.util.Iterator;

import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.StringUtils;
import jjj.asap.sas.weka.Dataset;
import jjj.asap.sas.weka.DatasetBuilder;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Imports the raw text files into ARFF format. The text is just stored in a string attribute.
 */
public class Import extends Job {

	public static void main(String[] args) throws Exception {
		Job job = new Import(args[0]);
		Job.log("ARGS",Arrays.toString(args));
		job.start();
	}

	private String tag;
	
	public Import(String tag) {
		super();
		this.tag = tag;
	}

	@Override
	protected void run() throws Exception {
		for(int k=1;k<=10;k++) {
			buildDataset(k, "work/text/t/"+k+"-"+tag+".txt","work/datasets/t/"+k+"-"+tag+".arff");
			buildDataset(k, "work/text/u/"+k+"-"+tag+".txt","work/datasets/u/"+k+"-"+tag+".arff");
		}
	}

	private void buildDataset(int k,String input, String output) {
		
		if(IOUtils.exists(output)) {
			Job.log("NOTE",output + " already exists - nothing to do.");
			return;
		}
		
		// create empty dataset
		final DatasetBuilder builder = new DatasetBuilder();
		builder.addVariable("id");
		if(Contest.isMultiChoice(k)) {
			builder.addNominalVariable("color", Contest.COLORS);
		}
		builder.addStringVariable("text");
		builder.addNominalVariable("score", Contest.getRubrics(k));
		Instances dataset = builder.getDataset(IOUtils.getName(output));
		
		// now add obs
		Iterator<String> it = new FileIterator(input);
		while(it.hasNext()) {
			// parse data
			String[] data = StringUtils.safeSplit(it.next(), "\t", 6);
			double id = Double.parseDouble(data[0]);
			String score = data[2];
			String color = data[4];
			String text = data[5];
			
			// add to dataset
			dataset.add(new DenseInstance(dataset.numAttributes()));
			Instance ob = dataset.lastInstance();
			ob.setValue(dataset.attribute("id"), id);
			if(Contest.isMultiChoice(k)) {
				ob.setValue(dataset.attribute("color"),color);
			}
			ob.setValue(dataset.attribute("text"), text);
			if("?".equals(score)) {
				ob.setValue(dataset.attribute("score"), Utils.missingValue());
			} else {
				ob.setValue(dataset.attribute("score"), score);
			}
		}
		
		Dataset.save(output, dataset);
	}
	
}
