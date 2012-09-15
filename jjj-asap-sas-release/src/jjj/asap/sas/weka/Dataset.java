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

package jjj.asap.sas.weka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jjj.asap.sas.util.Job;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Helper methods for dealing with Weka instances
 */
public class Dataset {

	/**
	 * Merges datasets into a new dataset and returns it
	 */
	public static Instances merge(Instances a, Instances b) {
		return Dataset.merge(new Instances[] {a,b});
	}

	/**
	 * Merges datasets into a new dataset and returns it
	 */
	public static Instances merge(List<Instances> datasets) {
		return Dataset.merge(datasets.toArray(new Instances[0]));
	}

	/**
	 * Merges datasets into a new dataset and returns it
	 */
	public static Instances merge(Instances[] datasets) {

		if(datasets == null || datasets.length <2) {
			throw new IllegalArgumentException("merge requires at least 2 datasets");
		}

		// Validate the input. The following rules are enforced:
		// 1. All datasets must have the first variable as "id"
		// and the last variable as "score"
		// 2. All datasets must have the same number of obs
		// 3. All datasets must have the same value in id and score
		// (after sorting on id)
		// 4. No duplicate variables except: id, color, score.

		// check lengths
		final int n = datasets[0].numInstances();
		for(int i=1;i<datasets.length;i++) {
			if(datasets[i].numInstances() != n) {
				throw new RuntimeException("merge: not all datasets have the same number of obs");
			}
		}

		// check variable names
		Set<String> vars = new HashSet<String>();
		for(int i=0;i<datasets.length;i++) {
			if(!"id".equals(datasets[i].attribute(0).name())) {
				throw new IllegalArgumentException("merge: " + datasets[i].relationName() + " has no id");
			}
			if(!"score".equals(datasets[i].attribute(datasets[i].numAttributes()-1).name())) {
				throw new IllegalArgumentException("merge: " + datasets[i].relationName() + " has no class variable");
			}
			for(int j=0;j<datasets[i].numAttributes();j++) {
				String name = datasets[i].attribute(j).name();
				if("color".equals(name)) continue;
				if("id".equals(name)) continue;
				if("score".equals(name)) continue;
				if(vars.contains(name)) {
					throw new RuntimeException("merge: duplicate variable name: "+name);
				}
				vars.add(name);
			}
		}

		// clone and sort
		Instances[] work = new Instances[datasets.length];
		for(int i=0;i<datasets.length;i++) {
			work[i] = new Instances(datasets[i]);
			work[i].sort(0);
		}

		// check values of id and class
		for(int k=0;k<work[0].numInstances();k++) {
			double id = work[0].instance(k).value(0);
			double score = work[0].instance(k).value(work[0].numAttributes()-1);
			for(int i=1;i<work.length;i++) {
				double id2 = work[i].instance(k).value(0);
				double score2 = work[i].instance(k).value(work[i].numAttributes()-1);
				if(!(Double.isNaN(score) && Double.isNaN(score2))) { // when both missing
					if(id != id2 || score != score2) {
						throw new RuntimeException(
								"merge: row: " + k 
								+ " id and class value differ between "
								+ work[0].relationName() + " and "
								+ work[i].relationName() 
								+ " expected " + id + "=" + score + " but found "
								+ id2 + "=" + score2
								);
					}
				}
			}
		}

		//
		// finally do the merge. We will be returning work[0]/
		// 

		// remove class var from work[0]
		work[0].setClassIndex(-1);
		work[0].deleteAttributeAt(work[0].numAttributes()-1);

		// now merge the middle. Might not be any if only two datasets
		for(int i=1;i<datasets.length-1;i++) {

			work[i].setClassIndex(-1);
			work[i].deleteAttributeAt(work[i].numAttributes()-1);
			work[i].deleteAttributeAt(0);

			Attribute color = work[i].attribute("color");
			if(color != null && work[0].attribute("color") != null) {
				work[i].deleteAttributeAt(color.index());
			}

			work[0] = Instances.mergeInstances(work[0], work[i]);
		}

		// finally add the right-most dataset

		Instances right = work[work.length-1];

		right.deleteAttributeAt(0);
		Attribute color = right.attribute("color");
		if(color != null && work[0].attribute("color") != null) {
			right.deleteAttributeAt(color.index());
		}
		work[0] = Instances.mergeInstances(work[0], right);

		String relName = datasets[0].relationName();
		for(int i=1;i<datasets.length;i++) {
			relName += "+"+datasets[i].relationName();
		}

		return work[0];
	}

	/**
	 * Loads a dataset
	 */
	public static Instances load(String filename) {
		try {
			Instances dataset = DataSource.read(filename);
			Job.log("DATASET","Loaded " + dataset.relationName() + " from " + filename);
			return dataset;
		} catch (Exception e) {
			throw new RuntimeException(filename,e);
		}
	}

	/**
	 * Saves a dataset to a file
	 */
	public static void save(String filename,Instances dataset) {
		try {
			DataSink.write(filename, dataset);
			Job.log("DATASET","Saved " + dataset.relationName() + " to " + filename);
		} catch (Exception e) {
			throw new RuntimeException(filename,e);
		}
	}

	/**
	 * @return a copy of the dataset but with a numeric class
	 */
	@Deprecated
	public static Instances makeClassNumeric(Instances dataset) {

		// might already be numeric
		if(dataset.attribute(dataset.numAttributes()-1).isNumeric()) {
			return new Instances(dataset);
		}

		// it's not numeric
		Instances copy = new Instances(dataset);
		copy.insertAttributeAt(new Attribute("__score__"), copy.numAttributes());

		int oldClassIndex = copy.attribute(copy.numAttributes()-2).index(); 
		int newClassIndex = copy.attribute(copy.numAttributes()-1).index();

		for(int i=0;i<copy.numInstances();i++) {
			Instance ob = copy.instance(i);
			if(ob.isMissing(oldClassIndex)) {
				ob.setMissing(newClassIndex);
			} else {
				String label = ob.stringValue(oldClassIndex);
				ob.setValue(newClassIndex, Double.parseDouble(label));
			}
		}

		copy.setClassIndex(copy.numAttributes()-1);
		copy.deleteAttributeAt(copy.numAttributes()-2);
		copy.renameAttribute(copy.numAttributes()-1, "score");

		return copy;
	}



}
