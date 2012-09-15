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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.RegressionModelBuilder;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.PLSClassifier;
import weka.classifiers.functions.SGD;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.REPTree;
import weka.core.SelectedTag;

/**
 * Builds a bucket of basic models
 */
public class RGramModels extends Job {

	private static final SelectedTag M5 = new SelectedTag(LinearRegression.SELECTION_M5,LinearRegression.TAGS_SELECTION);
	private static final SelectedTag NONE = new SelectedTag(LinearRegression.SELECTION_NONE,LinearRegression.TAGS_SELECTION);

	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new RGramModels(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();	
	}

	private String inputBucket;
	private String outputBucket;

	public RGramModels(String inputBucket, String outputBucket) {
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
		List<Classifier> models = new ArrayList<Classifier>(); 

		//SGD sgd = new SGD();
		//sgd.setDontNormalize(true);
		//sgd.setLossFunction(new SelectedTag(SGD.SQUAREDLOSS,SGD.TAGS_SELECTION));
		
		LinearRegression m5 = new LinearRegression();
		m5.setAttributeSelectionMethod(M5);
		
		//models.add(sgd);
		models.add(m5);
		
		LinearRegression lr = new LinearRegression();
		lr.setAttributeSelectionMethod(NONE);
				
		RandomSubSpace rss = new RandomSubSpace();
		rss.setClassifier(lr);
		rss.setNumIterations(30);
		
		models.add(rss);
		
		AdditiveRegression boostedStumps = new AdditiveRegression();
		boostedStumps.setClassifier(new DecisionStump());
		boostedStumps.setNumIterations(1000);
		
		AdditiveRegression boostedTrees = new AdditiveRegression();
		boostedTrees.setClassifier(new REPTree());
		boostedTrees.setNumIterations(100);
		
		models.add(boostedStumps);
		models.add(boostedTrees);
		
		models.add(new PLSClassifier());
		
		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// get the input from the bucket
		List<String> names = Bucket.getBucketItems("datasets", this.inputBucket);
		for(String dsn : names) {

			for(Classifier model : models) {

				String tag = null;
				if (model instanceof SingleClassifierEnhancer) {
					tag = model.getClass().getSimpleName() + "-" + ((SingleClassifierEnhancer)model).getClassifier().getClass().getSimpleName();
				} else {
					tag = model.getClass().getSimpleName();
				}
				
				queue.add(Job.submit(new RegressionModelBuilder(
						dsn,
						tag,
						AbstractClassifier.makeCopy(model),
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
