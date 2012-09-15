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
import weka.core.SelectedTag;
import weka.core.stemmers.LovinsStemmer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Creates a Bag of Words datasets using Alplabetical tokens.
 */
public class ForCosine extends Job {


	private static final SelectedTag DONT_NORMALIZE = 
			new SelectedTag(StringToWordVector.FILTER_NONE,StringToWordVector.TAGS_FILTER);
	private static final SelectedTag NORMALIZE = 
			new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL,StringToWordVector.TAGS_FILTER);


	/**
	 * args[0] is inputTag
	 * args[1] is output bucket
	 */
	public static void main(String[] args) throws Exception {
		Job job = new ForCosine(args[0],args[1]);
		Job.log("ARGS",Arrays.toString(args));
		job.start();
	}

	private String inputTag;
	private String outputBucket;

	public ForCosine(String inputTag,String outputBucket) {
		super();
		this.inputTag = inputTag;
		this.outputBucket = outputBucket;
	}

	@Override
	protected void run() throws Exception {

		// multi-threading
		Job.startService();
		Queue<Future<Object>> queue = new LinkedList<Future<Object>>();

		// essaySet,keep,stop,wc

		queue.add(Job.submit(
				new CosineDatasetBuilder(1,100,1,0,"spell-checked","cosine")
				));		
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(2,100,1,3,"spell-checked","cosine")
				));		
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(3,100,0,0,"spell-checked","cosine")
				));		
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(4,700,0,0,"spell-checked","cosine")
				));		
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(5,400,0,2,"spell-checked","cosine")
				));		

		queue.add(Job.submit(
				new CosineDatasetBuilder(6,200,0,0,"spell-checked","cosine")
				));	
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(7,500,1,3,"spell-checked","cosine")
				));	
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(8,300,2,0,"spell-checked","cosine")
				));	
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(9,200,1,0,"spell-checked","cosine")
				));	
		
		queue.add(Job.submit(
				new CosineDatasetBuilder(10,100,2,2,"spell-checked","cosine")
				));	
		
		// wait on complete
		Progress progress = new Progress(queue.size(),this.getClass().getSimpleName());
		while(!queue.isEmpty()) {
			queue.remove().get();
			progress.tick();
		}
		progress.done();
		Job.stopService();

	}

	private static class CosineDatasetBuilder implements Callable<Object> {

		private int essaySet;
		private int keep;
		private String inputTag;
		private String outputBucket;
		private int stop;
		private int wc;

		public CosineDatasetBuilder(int essaySet,int keep,int stop,int wc,String inputTag,String outputBucket) {
			this.essaySet = essaySet;
			this.keep = keep;
			this.inputTag = inputTag;
			this.outputBucket = outputBucket;
			this.stop = stop;
			this.wc = wc;
		}

		public Object call() throws Exception {

			String outputTag = "cosine";

			// train
			String input = "work/datasets/t/" + essaySet + "-"+inputTag+".arff";
			String output = "work/datasets/t/" + essaySet + "-"+inputTag+"-"+outputTag+".arff";

			// test
			String input2 = "work/datasets/u/" + essaySet + "-"+inputTag+".arff";
			String output2 = "work/datasets/u/" + essaySet + "-"+inputTag+"-"+outputTag+".arff";

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

			StringToWordVector nlp = getBOWFilter(essaySet, keep,stop,wc);

			Reorder order = new Reorder();
			if(!Contest.isMultiChoice(essaySet)) {
				order.setAttributeIndices("1,3-last,2");
			} else {
				order.setAttributeIndices("1,2,4-last,3");
			}

			MultiFilter filter = new MultiFilter(); 
			filter.setFilters(new Filter[]{
					nlp,
					order
			});
			Job.logWekaObject(filter);

			// apply filters and save dataset

			filter.setInputFormat($train);
			Instances train = Filter.useFilter($train, filter);	
			Instances test = Filter.useFilter($test, filter);

			final String name = IOUtils.getName(output);
			train.setRelationName(name);
			test.setRelationName(name);

			Bucket.add(train,this.outputBucket);
			Dataset.save(output,train);
			Dataset.save(output2,test);

			return null;
		}


		/**
		 * Creates a bag of words filter
		 */
		private StringToWordVector getBOWFilter(int essaySet,int keep,int stop,int wc ) throws Exception {


			WordTokenizer words = new WordTokenizer();

			StringToWordVector nlp = new StringToWordVector();
			nlp.setTokenizer(words);
			nlp.setAttributeNamePrefix("k_");

			switch(wc) {
			case 0:
				nlp.setOutputWordCounts(false);
				nlp.setTFTransform(false);
				nlp.setIDFTransform(false);
				break;
			case 1:
				nlp.setOutputWordCounts(true);
				nlp.setTFTransform(false);
				nlp.setIDFTransform(false);
				break;
			case 2:
				nlp.setOutputWordCounts(true);
				nlp.setTFTransform(true);
				nlp.setIDFTransform(false);
				break;
			case 3:
				nlp.setOutputWordCounts(true);
				nlp.setTFTransform(false);
				nlp.setIDFTransform(true);
				break;
			}

			nlp.setWordsToKeep(keep);
			nlp.setMinTermFreq(1);
			nlp.setStemmer(new LovinsStemmer());

			switch(stop) {
			case 0:
				nlp.setUseStoplist(false);
				break;
			case 1:
				nlp.setStopwords(new File("data\\stopwords.txt"));
				break;
			case 2:
				nlp.setUseStoplist(true);
				break;
			}

			nlp.setLowerCaseTokens(true);
			nlp.setNormalizeDocLength(DONT_NORMALIZE);
			nlp.setDoNotOperateOnPerClassBasis(true);

			return nlp;
		}



	}
}