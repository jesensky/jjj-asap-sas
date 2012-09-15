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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.FileIterator;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Timer;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * A crude implementation representing models.
 */
public class Model {

	public static Map<Double,Double> getPredictionsFromResiduals(int essaySet,Map<Double,Double> res) {
	
		double max = Contest.getRubrics(essaySet).size()-1;
		Map<Double,Double> preds = new HashMap<Double,Double>();
		
		for(Map.Entry<Double,Double> r : res.entrySet()) {
			double pred = Math.round(r.getValue());
			preds.put(r.getKey(), Calc.clamp(pred, 0,max));
		}
		
		return preds;
	}
	
	public static Map<Double,double[]> getProbabilties(int essaySet,Map<Double,Double> preds) {
		int n = Contest.getRubrics(essaySet).size();
		Map<Double,double[]> probs = new HashMap<Double,double[]>();
		for(Map.Entry<Double,Double> pred : preds.entrySet()) {
			double id = pred.getKey();
			int index = pred.getValue().intValue();
			double[] dist = new double[n];
			dist[index] = 1.0;
			probs.put(id,dist);
		}
		return probs;
	}
	
	
	/**
	 * @return a map of predictions based on the most likely outcome
	 */
	public static Map<Double,Double> getPredictions(int essaySet,Map<Double,double[]> probs) {

		Map<Double,Double> labels = new HashMap<Double,Double>();

		for(double id : probs.keySet()) {
			double[] dist = probs.get(id);
			double bestProb = -99999.99999;
			double bestRubric = -1;
			for(int rubric : Contest.getPriorRank(essaySet)) {
				if(dist[rubric] > bestProb) {
					bestProb = dist[rubric];
					bestRubric = rubric;
				}
			}

			labels.put(id,bestRubric);
		}

		return labels;
	}

	/**
	 * Loads saved probabilities
	 */
	public static Map<Double,double[]> loadProbabilities(final String filename) {
		Map<Double,double[]> model = new HashMap<Double,double[]>();
		Iterator<String> it = new FileIterator(filename);
		while(it.hasNext()) {
			String[] parts = it.next().split(",");
			double[] probs = new double[parts.length-1];
			for(int i=0;i<probs.length;i++) {
				probs[i] = Double.valueOf(parts[i+1]);
			}
			model.put(Double.valueOf(parts[0]), probs);
		}
		/////Job.log("MODEL", "Loaded probabilities from " + filename);
		return model;
	}

	/**
	 * Saves probabilities
	 */
	public static void saveProbabilities(final String filename,Map<Double,double[]> model) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			for(Map.Entry<Double, double[]> prediction : model.entrySet()) {
				writer.print(prediction.getKey());
				for(double x : prediction.getValue()) {
					writer.print(",");
					writer.print(x);
				}
				writer.println();
			}
			writer.flush();
			writer.close();
			Job.log("MODEL", "Saved probabilities to " + filename);
		} catch(Exception e) {
			throw new RuntimeException(filename,e);
		}

	}

	/**
	 * Loads saved predictions
	 */
	public static Map<Double,Double> loadPredictions(final String filename) {
		Map<Double,Double> labels = new HashMap<Double,Double>();
		Iterator<String> it = new FileIterator(filename);
		while(it.hasNext()) {
			String[] parts = it.next().split(",");
			labels.put(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
		}
		//Job.log("MODEL", "Loaded labels from " + filename);
		return labels;
	}

	/**
	 * Saves predictions
	 */
	public static void savePredictions(final String filename,Map<Double,Double> labels) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			for(Map.Entry<Double, Double> label : labels.entrySet()) {
				writer.println(label.getKey() + "," + label.getValue());
			}
			writer.flush();
			writer.close();
			Job.log("MODEL", "Saved labels to " + filename);
		} catch(Exception e) {
			throw new RuntimeException(filename,e);
		}

	}

	/**
	 * 
	 */
	private static ConcurrentMap<String,Loader> loaders = new ConcurrentHashMap<String,Loader>();

	/**
	 * @throws Exception 
	 * 
	 */
	public static Map<Double,double[]> loadCachedModel(final String name) {

		try {
			Loader loader = loaders.putIfAbsent(name, new Loader());
			if(loader == null) loader = loaders.get(name);

			return loader.load(name);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Loader {

		public synchronized Map<Double,double[]> load(final String name) throws Exception {

			// first try to find the saved model
			String probsFileName = "work/models1/u/" + name;
			if(IOUtils.exists(probsFileName)) {
				return Model.loadProbabilities(probsFileName);
			}

			//
			// no saved copy exists. Need to train the serialized model
			//

			String[] parts = name.split("@");
			String dsn = parts[0];
			String tag = parts[1];

			// load the datasets
			Instances train = Dataset.load("work/datasets/t/" + dsn);
			Instances test = Dataset.load("work/datasets/u/" + dsn);
			
			
			String $model = "work/models1/t/" + dsn + "@" + tag + "@model";
			String $trainedModel = "work/models1/u/" + dsn + "@" + tag + "@model";

			boolean alreadyTrained = false;
			Classifier classifier = null;
			if(IOUtils.exists($trainedModel)) {
				alreadyTrained = true;
				classifier = (Classifier) SerializationHelper.read($trainedModel);
			} else {
				classifier = (Classifier) SerializationHelper.read($model);
			}
			
			Timer timer = new Timer();

			boolean hasNumericClass = train.attribute(train.numAttributes()-1).isNumeric();
			
			Map<Double,double[]> probs = null;
			
			// train it
			if(hasNumericClass) {
				if(!alreadyTrained) {
					Weka.trainRegressor(train, classifier);
					SerializationHelper.write($trainedModel, classifier);
				}
				Map<Double,Double> res = Weka.regClassifyInstances(test, classifier);
				int essaySet = Contest.getEssaySet(dsn);
				Map<Double,Double> preds = Model.getPredictionsFromResiduals(essaySet, res);
				probs = Model.getProbabilties(essaySet, preds);
				Model.savePredictions(probsFileName.replaceFirst("@probs", "@res"), res);
			} else {
				if(!alreadyTrained) {
					Weka.trainClassifier(train, classifier);
					SerializationHelper.write($trainedModel, classifier);
				}
				probs = Weka.classifyInstances(test, classifier);
			}

			// Get the labels (probs)

			Job.log("NOTE", "Training " + $model + " took " + timer.getElapsedSeconds() + " secs.");

			// save and return
			Model.saveProbabilities(probsFileName, probs);
			return probs;
		}

	}

	/**
	 * save the residuals
	 * @param filename
	 * @param res
	 */
	public static void saveResiduals(String filename,
			Map<Double, Double> res) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			for(Map.Entry<Double, Double> label : res.entrySet()) {
				writer.println(label.getKey() + "," + label.getValue());
			}
			writer.flush();
			writer.close();
			Job.log("MODEL", "Saved residuals to " + filename);
		} catch(Exception e) {
			throw new RuntimeException(filename,e);
		}

	}


}
