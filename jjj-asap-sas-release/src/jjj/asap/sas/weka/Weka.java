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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Job;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Helper methods for working with Weka
 */
public class Weka {

	/**
	 * k-fold cross validation
	 */
	public static Map<Double,double[]> crossValidate(Instances dataset,Classifier prototype,int numFolds,int seed) throws Exception {

		// init
		Map<Double,double[]> probs = new HashMap<Double,double[]>();

		// prepare data
		Instances work = new Instances(dataset);
		work.sort(0);
		work.randomize(new Random(seed));
		work.setClassIndex(work.numAttributes()-1);
		if(seed < 1000) { // hack
			work.stratify(numFolds);
		}
		
		for(int k=0;k<numFolds;k++) {

			// get data for fold
			Instances train = work.trainCV(numFolds, k);

			// add additional training examples that were scored by the second expert.

			Instances extra = new Instances(train);
			extra.setClassIndex(extra.numAttributes()-1);

			Map<Double,String> extraLabels = Contest.getSilverStandard(0);

			for(int i=0;i<extra.numInstances();i++) {
				final Instance ob = extra.instance(i);
				final double id = ob.value(0);
				ob.setClassValue(extraLabels.get(id));
				train.add(ob);
			}

			// end of adding additional training examples

			prepare(train);
			Instances test = work.testCV(numFolds, k);

			Classifier classifier = AbstractClassifier.makeCopy(prototype);
			classifier.buildClassifier(train);

			probs.putAll(classifyInstances(test,classifier));
		}

		return probs;		
	}

	/**
	 * k-fold cross validation using Regression
	 */
	public static Map<Double,Double> regCrossValidate(Instances dataset,Classifier prototype,int numFolds,int seed) throws Exception {

		// init
		Map<Double,Double> res = new HashMap<Double,Double>();

		// Make dataset numeric
		//Instances regDataset = Dataset.makeClassNumeric(dataset);
		
		// prepare data
		//Instances work = new Instances(regDataset);
		Instances work = new Instances(dataset);

		work.sort(0);
		work.randomize(new Random(seed));
		work.setClassIndex(work.numAttributes()-1);
		
		for(int k=0;k<numFolds;k++) {

			// get data for fold
			Instances train = work.trainCV(numFolds, k);

			// add additional training examples that were scored by the second expert.

			Instances extra = new Instances(train);
			extra.setClassIndex(extra.numAttributes()-1);

			Map<Double,String> extraLabels = Contest.getSilverStandard(0);

			for(int i=0;i<extra.numInstances();i++) {
				final Instance ob = extra.instance(i);
				final double id = ob.value(0);
				ob.setClassValue(Double.parseDouble(extraLabels.get(id)));
				train.add(ob);
			}

			// end of adding additional training examples

			prepare(train);
			Instances test = work.testCV(numFolds, k);

			Classifier classifier = AbstractClassifier.makeCopy(prototype);
			classifier.buildClassifier(train);

			res.putAll(regClassifyInstances(test,classifier));

		}

		return res;		
	}
	
	/**
	 * @return id -> residual 
	 * @throws Exception 
	 */
	public static Map<Double,Double> regClassifyInstances(
			Instances test, Classifier classifier) throws Exception {
		
		Map<Double,Double> res = new HashMap<Double,Double>();

		Instances work = new Instances(test);
		prepare(work);

		for(int i=0;i<test.numInstances();i++) {
			double[] dist = classifier.distributionForInstance(work.instance(i));
			double id = test.instance(i).value(0);
			res.put(id, dist[0]);
		}

		return res;
	}

	/**
	 * k-fold cross validation
	 */
	public static Map<Double,double[]> crossValidate(Instances dataset,Classifier prototype,int numFolds) throws Exception {
		return crossValidate(dataset,prototype,numFolds,1);	
	}

	/**
	 * Labels instances using a trained classifier
	 */
	public static Map<Double,double[]> classifyInstances(Instances unlabeled,Classifier trainedClassifier) throws Exception {

		Map<Double,double[]> labels = new HashMap<Double,double[]>();

		Instances work = new Instances(unlabeled);
		prepare(work);

		for(int i=0;i<unlabeled.numInstances();i++) {
			double[] probs = null;
			try {
				probs = trainedClassifier.distributionForInstance(work.instance(i));
			} catch(Exception e) {
				System.out.println(work.instance(i));
				throw e;
			}
			double id = unlabeled.instance(i).value(0);
			//System.out.println(work.instance(i));
			//System.out.println(i + " " + id + " => " + Arrays.toString(probs));
			labels.put(id, probs);

		}

		return labels;
	}

	public static void trainClassifier(Instances dataset,Classifier classifier) throws Exception {

		// prepare data
		Instances work = new Instances(dataset);

		// add additional training examples that were scored by the second expert.

		Instances extra = new Instances(work);
		extra.setClassIndex(extra.numAttributes()-1);

		Map<Double,String> extraLabels = Contest.getSilverStandard(0);

		for(int i=0;i<extra.numInstances();i++) {
			final Instance ob = extra.instance(i);
			final double id = ob.value(0);
			ob.setClassValue(extraLabels.get(id));
			work.add(ob);
		}

		// end of adding additional training examples

		prepare(work);				

		// train
		classifier.buildClassifier(work);

	}
	
	
	public static void trainRegressor(Instances dataset,Classifier regressor) throws Exception {

		// prepare data
		Instances work = new Instances(dataset);

		// add additional training examples that were scored by the second expert.

		Instances extra = new Instances(work);
		extra.setClassIndex(extra.numAttributes()-1);

		Map<Double,String> extraLabels = Contest.getSilverStandard(0);

		for(int i=0;i<extra.numInstances();i++) {
			final Instance ob = extra.instance(i);
			final double id = ob.value(0);
			ob.setClassValue(Double.parseDouble(extraLabels.get(id)));
			work.add(ob);
		}

		// end of adding additional training examples

		prepare(work);				

		// train
		regressor.buildClassifier(work);

	}

	public static void trainClassifierOnDataset(Instances dataset,Classifier classifier) throws Exception {

		// prepare data
		Instances work = new Instances(dataset);
		prepare(work);				

		// train
		classifier.buildClassifier(work);

	}

	public static void trainRegressorOnDataset(Instances dataset,Classifier regressor) throws Exception {

		// prepare data
		Instances work = new Instances(dataset);
		prepare(work);				

		// train
		regressor.buildClassifier(work);

	}
	
	/**
	 * Prepares a dataset for machine learning.
	 * 1. Removes ID
	 * 2. Sets the last attribute as the class
	 * @param dataset the dataset edited in place
	 */
	public static void prepare(Instances dataset) {
		dataset.deleteAttributeAt(0);
		dataset.setClassIndex(dataset.numAttributes()-1);
	}



}
