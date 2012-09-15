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
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.net.estimate.BMAEstimator;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.SelectedTag;

/**
 * Builds a bucket of basic models
 */
public class BuildRBFKernelModels extends Job {

	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new BuildRBFKernelModels(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();	
	}

	private String inputBucket;
	private String outputBucket;

	public BuildRBFKernelModels(String inputBucket, String outputBucket) {
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

		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// get the input from the bucket
		List<String> names = Bucket.getBucketItems("datasets", this.inputBucket);
		for(String dsn : names) {

			SMO smo = new SMO();
			smo.setFilterType(new SelectedTag(SMO.FILTER_NONE,SMO.TAGS_FILTER));
			smo.setBuildLogisticModels(true);
			RBFKernel kernel = new RBFKernel();
			kernel.setGamma(0.05);
			smo.setKernel(kernel);

			AttributeSelectedClassifier asc = new AttributeSelectedClassifier();
			asc.setEvaluator(new InfoGainAttributeEval());
			Ranker ranker = new Ranker();
			ranker.setThreshold(0.01);
			asc.setSearch(ranker);
			asc.setClassifier(smo);

			queue.add(Job.submit(new ModelBuilder(
					dsn,
					"InfoGain-SMO-RBFKernel",
					asc,
					this.outputBucket)));
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
