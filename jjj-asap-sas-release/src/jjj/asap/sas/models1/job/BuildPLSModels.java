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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.RegressionModelBuilder;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.PLSClassifier;
import weka.core.SelectedTag;
import weka.filters.supervised.attribute.PLSFilter;

/**
 * Builds a bucket of basic models
 */
public class BuildPLSModels extends Job {

	private static final SelectedTag CENTER = new SelectedTag(PLSFilter.PREPROCESSING_CENTER,PLSFilter.TAGS_PREPROCESSING);
	private static final SelectedTag NONE = new SelectedTag(PLSFilter.PREPROCESSING_NONE,PLSFilter.TAGS_PREPROCESSING);
	private static final SelectedTag STANDARDIZE = new SelectedTag(PLSFilter.PREPROCESSING_STANDARDIZE,PLSFilter.TAGS_PREPROCESSING);


	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new BuildPLSModels(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();	
	}

	private String inputBucket;
	private String outputBucket;

	public BuildPLSModels(String inputBucket, String outputBucket) {
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

		// Standard PLS

		PLSClassifier pls = new PLSClassifier();
		PLSFilter filter = (PLSFilter)pls.getFilter();
		filter.setNumComponents(5);
		filter.setPreprocessing(NONE);

		// centered PLS

		PLSClassifier plsc = new PLSClassifier();
		PLSFilter center = (PLSFilter)plsc.getFilter();
		center.setNumComponents(5);
		center.setPreprocessing(CENTER);

		// standardized PLS

		PLSClassifier plss = new PLSClassifier();
		PLSFilter std = (PLSFilter)plss.getFilter();
		std.setNumComponents(10);
		std.setPreprocessing(STANDARDIZE);

		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// get the input from the bucket
		List<String> names = Bucket.getBucketItems("datasets", this.inputBucket);
		for(String dsn : names) {

			int essaySet = Contest.getEssaySet(dsn);

			Classifier alg = pls; 

			if(essaySet == 10 || dsn.contains("1grams-thru-3grams")) {
				alg=plsc;
			}

			if(essaySet == 7) {
				alg=plss;
			}
			
			queue.add(Job.submit(new RegressionModelBuilder(
					dsn,
					"PLS",
					AbstractClassifier.makeCopy(alg),
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
