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

package jjj.asap.sas.models2.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

import jjj.asap.sas.ensemble.EnsembleBuilder;
import jjj.asap.sas.ensemble.LoaderL1;
import jjj.asap.sas.ensemble.impl.CrossValidatedEnsemble;
import jjj.asap.sas.ensemble.impl.StackedClassifier;
import jjj.asap.sas.ensemble.scheme.GACommittee;
import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.core.SelectedTag;

/**
 * Builds a variety of classifiers
 */

public class BuildUnicornEnsemble extends Job {

	private static final SelectedTag M5 = new SelectedTag(LinearRegression.SELECTION_M5,LinearRegression.TAGS_SELECTION);
	private static final SelectedTag NONE = new SelectedTag(LinearRegression.SELECTION_NONE,LinearRegression.TAGS_SELECTION);

	/**
	 * @param args[0] is the output bucket
	 * 
	 * @param args[1] the output name. Actually outputs will be:
	 * work/models2/t/k-name.csv and work/models2/u/k-name.csv. These will be
	 * in submission format, though possibly not in the correct order.
	 * 
	 * @param args[2],args[3],... one or more input buckets. If more than one
	 * bucket is given and duplicate items are found, only one will be processed.
	 * 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		// check
		if(args.length < 3) {
			throw new RuntimeException(Arrays.toString(args));
		}

		// input array
		String[] inputBuckets = new String[args.length-2];
		System.arraycopy(args, 2, inputBuckets, 0, inputBuckets.length);

		Job job = new BuildUnicornEnsemble(args[0],args[1],inputBuckets);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();

	}

	private String outputBucket;
	private String outputName;
	private String[] inputBuckets;



	public BuildUnicornEnsemble(String outputBucket, String outputName,
			String[] inputBuckets) {
		super();
		this.outputBucket = outputBucket;
		this.outputName = outputName;
		this.inputBuckets = inputBuckets;
	}



	@Override
	protected void run() throws Exception {

		// get all the items from the buckets
		Set<String> items = new HashSet<String>();
		for(String inputBucket : inputBuckets) {
			items.addAll(Bucket.getBucketItems("models", inputBucket));
		}

		// split the buckets up by essay set
		List<String>[] buckets = new List[10];
		for(int i=0;i<10;i++) {
			buckets[i] = new ArrayList<String>();
		}

		for(String item : items) {
			int index = Contest.getEssaySet(item)-1;
			buckets[index].add(item);
		}

		// init multi-threading
		Job.startService();
		final Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// Stacking over labels

		for(int k=0;k<10;k++) {

			int essaySet = k+1;

			queue.add(Job.submit(new EnsembleBuilder(
					essaySet,
					this.outputBucket,
					this.outputName + "-naive-bayes",
					new GACommittee(10,30,
							new CrossValidatedEnsemble(
									new StackedClassifier(true,new NaiveBayes()),
									5
									),
									60
							),
							buckets[k],
							new LoaderL1()
					)));
		}

		// wait on complete
		Progress progress = new Progress(queue.size(),this.getClass().getSimpleName());
		while(!queue.isEmpty()) {
			try {
				queue.remove().get();
			} catch(Exception e) {
				Job.log("ERROR", e.toString());
				e.printStackTrace(System.err);
			}
			progress.tick();
		}
		progress.done();
		Job.stopService();

	}

}
