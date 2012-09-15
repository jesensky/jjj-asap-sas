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

package jjj.asap.sas.ensemble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import jjj.asap.sas.ensemble.WeakLearner.Loader;
import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Timer;
import jjj.asap.sas.weka.Model;

/**
 * Takes as inputs a collection of weak learners and a scheme.
 * Outputs the best performing ensemble of the weak learners.
 */
public class EnsembleBuilder implements Callable<Object> {

	private int essaySet;
	private String ensembleBucket;
	private String ensembleName;
	private Scheme scheme;
	private List<String> bucket;
	private WeakLearner.Loader loader;

	/**
	 * Constructs the ensemble builder
	 * @param essaySet
	 * @param name
	 * @param ensemble
	 * @param bucket
	 * @param loader
	 */
	public EnsembleBuilder(int essaySet, String ensembleBucket, String ensembleName, Scheme scheme,
			List<String> bucket, Loader loader) {
		super();
		this.essaySet = essaySet;
		this.ensembleBucket = ensembleBucket;
		this.ensembleName = ensembleName;
		this.scheme = scheme;
		this.bucket = bucket;
		this.loader = loader;
	}

	@Override
	public Double call() throws Exception {

		// init I/O

		final String $train = "work/models2/t/"+essaySet+"-"+this.ensembleName+".csv";
		final String $test = "work/models2/u/"+essaySet+"-"+this.ensembleName+".csv";

		if(IOUtils.exists($train) && IOUtils.exists($test)) {
			Job.log("NOTE",$train + " already exists - nothing to do.");
			Job.log("NOTE",$test + " already exists - nothing to do.");
			return null;
		} else {
			Job.log("NOTE", "Building " + ensembleName + " for essay set #" + essaySet);
		}
		Timer timer = new Timer();
		
		// load the learners to use for training
		List<WeakLearner> training = new ArrayList<WeakLearner>();
		for(String name : this.bucket) {
			training.add(this.loader.loadTraining(name));
		}
		
		// sort learners so the result is repeatable
		Collections.sort(training,new Comparator<WeakLearner>() {
			@Override
			public int compare(WeakLearner arg0, WeakLearner arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});

		// train the ensemble classifier
		StrongLearner classifier = this.scheme.build(essaySet,ensembleName,training);

		// load the learners to use for classifying
		List<WeakLearner> classifiers = new ArrayList<WeakLearner>();
		for(WeakLearner weakLearner: classifier.getLearners()) {
			classifiers.add(this.loader.loadTest(weakLearner.getName()));
		}

		// make predictions
		Map<Double,Double> predictions = this.scheme.classify(essaySet,ensembleName,classifiers,classifier.getContext());

		// save files
		Model.savePredictions($train, classifier.getPreds());
		Model.savePredictions($test, predictions);
		
		// add to bucket
		StringBuilder buffer = new StringBuilder();
		for(WeakLearner learner : classifier.getLearners()) {
			buffer.append(learner.getKappa());
			buffer.append(",");
			buffer.append(learner.getName());
			buffer.append("\n");
		}
		buffer.append(classifier.getKappa());
		buffer.append(",NET KAPPA\n");
		if(classifier.getContext() != null) {
			if(classifier.getContext() instanceof double[]) {
				buffer.append(Arrays.toString((double[])classifier.getContext()));
			} else if(classifier.getContext() instanceof String[]) {
				buffer.append(Arrays.toString((String[])classifier.getContext()));
			} else if(classifier.getContext() instanceof int[]) {
				buffer.append(Arrays.toString((int[])classifier.getContext()));
			} else {
				buffer.append(classifier.getContext().toString());
			}
		}
		Bucket.add("ensembles", this.ensembleBucket, IOUtils.getName($train), buffer.toString());

		Job.log("NOTE", "Completed " + ensembleName + " for essay set #" + essaySet + " (" + timer.getElapsedSeconds() + " secs)");
		
		return null;
		
	}


}
