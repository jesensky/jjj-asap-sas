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

package jjj.asap.sas.models1.job;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.ModelBuilder;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.REPTree;

/**
 * Builds a bucket of basic models
 */
public class BuildBasicMetaCostModels extends Job {

	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new BuildBasicMetaCostModels(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();	
	}

	private String inputBucket;
	private String outputBucket;

	public BuildBasicMetaCostModels(String inputBucket, String outputBucket) {
		super();
		this.inputBucket = inputBucket;
		this.outputBucket = outputBucket;
	}

	@Override
	protected void run() throws Exception {

		// validate args
		if(!Bucket.isBucket("datasets",inputBucket)) {
			throw new FileNotFoundException(inputBucket);
		}
		if(!Bucket.isBucket("models",outputBucket)) {
			throw new FileNotFoundException(outputBucket);
		}

		// create prototype classifiers
		Map<String,Classifier> prototypes = new HashMap<String,Classifier>();

		// Bagged REPTrees
		
		Bagging baggedTrees = new Bagging();
		baggedTrees.setNumExecutionSlots(1);
		baggedTrees.setNumIterations(100);
		baggedTrees.setClassifier(new REPTree());
		baggedTrees.setCalcOutOfBag(false);

		prototypes.put("Bagged-REPTrees", baggedTrees);

		// Bagged SMO
		
		Bagging baggedSVM = new Bagging();
		baggedSVM.setNumExecutionSlots(1);
		baggedSVM.setNumIterations(100);
		baggedSVM.setClassifier(new SMO());
		baggedSVM.setCalcOutOfBag(false);

		prototypes.put("Bagged-SMO", baggedSVM);
		
		// Meta Cost model for Naive Bayes

		Bagging bagging = new Bagging();
		bagging.setNumExecutionSlots(1);
		bagging.setNumIterations(100);
		bagging.setClassifier(new NaiveBayes());

		CostSensitiveClassifier meta = new CostSensitiveClassifier();
		meta.setClassifier(bagging);	
		meta.setMinimizeExpectedCost(true);

		prototypes.put("CostSensitive-MinimizeExpectedCost-NaiveBayes", bagging);
			
		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// get the input from the bucket
		List<String> names = Bucket.getBucketItems("datasets", this.inputBucket);
		for(String dsn : names) {

			// for each prototype classifier
			for(Map.Entry<String, Classifier> prototype : prototypes.entrySet()) {

				
				// 
				// speical logic for meta cost
				//
				
				Classifier alg = AbstractClassifier.makeCopy(prototype.getValue());
				
				if(alg instanceof CostSensitiveClassifier) {
					
					int essaySet = Contest.getEssaySet(dsn);
					
					String matrix = Contest.getRubrics(essaySet).size() == 3? "cost3.txt":"cost4.txt";

					((CostSensitiveClassifier)alg).setCostMatrix(new CostMatrix(new FileReader("/asap/sas/trunk/"+matrix)));	
					
				} 
				
				// use InfoGain to discard useless attributes

				AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();

				classifier.setEvaluator(new InfoGainAttributeEval());

				Ranker ranker = new Ranker();
				ranker.setThreshold(0.0001);
				classifier.setSearch(ranker);

				classifier.setClassifier(alg);

				queue.add(Job.submit(new ModelBuilder(
						dsn,
						"InfoGain-"+prototype.getKey(),
						classifier,
						this.outputBucket)));
			}
		}

		// wait on complete
		Progress progress = new Progress(queue.size(),this.getClass().getSimpleName());
		while(!queue.isEmpty()) {
			try {
				queue.remove().get();
			} catch(Exception e) {
				Job.log("ERROR", e.toString());
			}
			progress.tick();
		}
		progress.done();
		Job.stopService();

	}

}
