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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.ModelBuilder;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.NBTree;

/**
 * Builds a bucket of basic models
 */
public class BuildBasicModels2 extends Job {

	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new BuildBasicModels2(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();	
	}

	private String inputBucket;
	private String outputBucket;

	public BuildBasicModels2(String inputBucket, String outputBucket) {
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

		// models

		prototypes.put("NBTree", new NBTree());
		prototypes.put("Logistic", new Logistic());

		
		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// get the input from the bucket
		List<String> names = Bucket.getBucketItems("datasets", this.inputBucket);
		for(String dsn : names) {

			// for each prototype classifier
			for(Map.Entry<String, Classifier> prototype : prototypes.entrySet()) {

				// use InfoGain to discard useless attributes

				AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();

				classifier.setEvaluator(new InfoGainAttributeEval());

				Ranker ranker = new Ranker();
				ranker.setThreshold(0.0001);
				classifier.setSearch(ranker);

				classifier.setClassifier(AbstractClassifier.makeCopy(prototype.getValue()));

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
