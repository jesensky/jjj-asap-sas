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
import jjj.asap.sas.weka.CosineDistance;
import jjj.asap.sas.weka.ModelBuilder;
import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import weka.core.neighboursearch.LinearNNSearch;

/**
 * Builds Cosine models
 */
public class BuildCosineModels extends Job {

	private static final SelectedTag INVERSE = new SelectedTag(IBk.WEIGHT_INVERSE,IBk.TAGS_WEIGHTING);
	private static final SelectedTag NONE = new SelectedTag(IBk.WEIGHT_NONE,IBk.TAGS_WEIGHTING);

	
	/**
	 * args[0] - input bucket
	 * args[1] - output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new BuildCosineModels(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();
	}

	private String inputBucket;
	private String outputBucket;

	public BuildCosineModels(String inputBucket, String outputBucket) {
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

			int essaySet = Contest.getEssaySet(dsn);
			
			int k=-1;
			switch(essaySet) {
			
			case 3:
				k=13;
				break;
			case 5:
			case 7:
				k=55;
				break;
			case 2:
			case 6:
			case 10:
				k=21;
				break;
			case 1:
			case 4:
			case 8:
			case 9:
				k=34;
				break;
			}
			
			if(k==-1) {
				throw new IllegalArgumentException("not k defined for "+essaySet);
			}
			
			LinearNNSearch search = new LinearNNSearch();
			search.setDistanceFunction(new CosineDistance());
			search.setSkipIdentical(false);

			IBk knn = new IBk();
			knn.setKNN(k);
			knn.setDistanceWeighting(INVERSE);
			knn.setNearestNeighbourSearchAlgorithm(search);

			queue.add(Job.submit(new ModelBuilder(
					dsn,
					"KNN-"+k,
					knn,
					this.outputBucket)));
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
