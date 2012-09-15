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

package jjj.asap.sas.datasets.job;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.Dataset;
import weka.core.Instances;
import weka.core.stemmers.LovinsStemmer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Creates a Bag of Words datasets using Alplabetical tokens.
 */
public class ForRegression extends Job {

	/**
	 * args[0] is inputTag
	 * args[1] is output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new ForRegression(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));
		job.start();
	}

	private String inputTag;
	private String outputBucket;

	public ForRegression(String inputTag,String outputBucket) {
		super();
		this.inputTag = inputTag;
		this.outputBucket = outputBucket;
	}

	@Override
	protected void run() throws Exception {

		// multi-threading
		Job.startService();
		Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// K MTF IN OUT WORDS min-N max-N

		for(int k=1;k<=10;k++) {
			queue.add(Job.submit(new RegDatasetBuilder(k, (k==5 || k == 6) ? 120 : 160, inputTag, outputBucket)));
		}

		// wait on complete
		Progress progress = new Progress(queue.size(),this.getClass().getSimpleName());
		while(!queue.isEmpty()) {
			queue.remove().get();
			progress.tick();
		}
		progress.done();
		Job.stopService();

	}

	private static class RegDatasetBuilder implements Callable<Object> {

		private int k;
		private int keep;
		private String inputTag;
		private String outputBucket;

		public RegDatasetBuilder(int k, int keep, String inputTag,
				String outputBucket) {
			super();
			this.k = k;
			this.keep = keep;
			this.inputTag = inputTag;
			this.outputBucket = outputBucket;
		}

		public Object call() throws Exception {

			String outputTag = "reg-keep" + keep + "-extra-stats";

			// train
			String input = "work/datasets/t/" + k + "-"+inputTag+".arff";
			String output = "work/datasets/t/" + k + "-"+inputTag+"-"+outputTag+".arff";

			// test
			String input2 = "work/datasets/u/" + k + "-"+inputTag+".arff";
			String output2 = "work/datasets/u/" + k + "-"+inputTag+"-"+outputTag+".arff";

			if(IOUtils.exists(output) && IOUtils.exists(output2)) {
				Job.log("NOTE",output + " already exists - nothing to do.");
				Job.log("NOTE",output2 + " already exists - nothing to do.");
				return null;
			}

			// load input

			Instances $train = Dataset.load(input);
			Instances $test = Dataset.load(input2);

			$train.setClassIndex($train.numAttributes()-1);
			$test.setClassIndex($test.numAttributes()-1);

			// create filters

			StringToWordVector nlp = new StringToWordVector();


			nlp.setTokenizer(new WordTokenizer());

			nlp.setAttributeNamePrefix("a_");
			nlp.setOutputWordCounts(false);
			nlp.setMinTermFreq(1);
			nlp.setWordsToKeep(keep);
			nlp.setStemmer(new LovinsStemmer());

			nlp.setStopwords(new File("data\\stopwords.txt"));

			nlp.setTFTransform(false);
			nlp.setIDFTransform(false);
			nlp.setLowerCaseTokens(true);

			Reorder order = new Reorder();
			if(!Contest.isMultiChoice(k)) {
				order.setAttributeIndices("1,3-last,2");
			} else {
				order.setAttributeIndices("1,2,4-last,3");
			}

			MultiFilter filter = new MultiFilter(); 
			filter.setFilters(new Filter[]{
					nlp,
					order,
					new NominalToBinary()
			});
			Job.logWekaObject(filter);

			// apply filters and save dataset

			filter.setInputFormat($train);
			Instances train = Filter.useFilter($train, filter);	
			Instances test = Filter.useFilter($test, filter);

			// add extra stats

			Instances trainExtraStats = Dataset.load("work/datasets/t/" + k + "-extra-stats.arff");
			Instances testExtraStats = Dataset.load("work/datasets/u/" + k + "-extra-stats.arff");

			train = Dataset.makeClassNumeric(Dataset.merge(train, trainExtraStats));
			test = Dataset.makeClassNumeric(Dataset.merge(test, testExtraStats));

			final String name = IOUtils.getName(output);
			train.setRelationName(name);
			test.setRelationName(name);

			Bucket.add(train,this.outputBucket);
			Dataset.save(output,train);
			Dataset.save(output2,test);

			return null;
		}
	}
}