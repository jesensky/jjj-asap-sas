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

package jjj.asap.sas.ensemble.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jjj.asap.sas.ensemble.Ensemble;
import jjj.asap.sas.ensemble.Scheme;
import jjj.asap.sas.ensemble.StrongLearner;
import jjj.asap.sas.ensemble.WeakLearner;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.weka.DatasetBuilder;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * A meta ensemble that uses cross validation to estimate the worth of the
 * wrapped ensemble.
 */
public class CrossValidatedEnsemble implements Scheme {

	private Ensemble ensemble;
	private int nFolds;
	
	/**
	 * @param ensemble based ensemble
	 * @param nFolds how many cv folds
	 */
	public CrossValidatedEnsemble(Ensemble ensemble, int nFolds) {
		super();
		this.ensemble = ensemble;
		this.nFolds = nFolds;
	}

	@Override
	public StrongLearner build(int essaySet, String ensembleName,
			List<WeakLearner> learners) {

		// can't handle empty case
		if(learners.isEmpty()) {
			return this.ensemble.build(essaySet, ensembleName, learners);
		}
		
		// create a dummy dataset.
		DatasetBuilder builder = new DatasetBuilder();
		builder.addVariable("id");
		builder.addNominalVariable("class", Contest.getRubrics(essaySet));
		Instances dummy = builder.getDataset("dummy");
		
		// add data
		Map<Double,Double> groundTruth = Contest.getGoldStandard(essaySet);
		for(double id : learners.get(0).getPreds().keySet()) {
			dummy.add(new DenseInstance(1.0,new double[] { id,groundTruth.get(id) } ));
		}
		
		// stratify
		dummy.sort(0);
		dummy.randomize(new Random(1));
		dummy.setClassIndex(1);
		dummy.stratify(nFolds);

		// now evaluate each fold
		Map<Double,Double> preds = new HashMap<Double,Double>();
		for(int k=0;k<nFolds;k++) {
			Instances train = dummy.trainCV(nFolds, k);
			Instances test = dummy.testCV(nFolds, k);
			
			List<WeakLearner> cvLeaners = new ArrayList<WeakLearner>();
			for(WeakLearner learner : learners) {
				WeakLearner copy = learner.copyOf();
				for(int i=0;i<test.numInstances();i++) {
					copy.getPreds().remove(test.instance(i).value(0));
					copy.getProbs().remove(test.instance(i).value(0));
				}
				cvLeaners.add(copy);
			}
		
			// train on fold
			StrongLearner cv = this.ensemble.build(essaySet, ensembleName, cvLeaners);
			
			List<WeakLearner> testLeaners = new ArrayList<WeakLearner>();
			for(WeakLearner learner : cv.getLearners()) {
				WeakLearner copy = learner.copyOf();
				copy.getPreds().clear();
				copy.getProbs().clear();
				WeakLearner source = find(copy.getName(),learners);
				for(int i=0;i<test.numInstances();i++) {
					double id = test.instance(i).value(0);
					copy.getPreds().put(id,source.getPreds().get(id));
					copy.getProbs().put(id,source.getProbs().get(id));
				}
				testLeaners.add(copy);
			}
			
			preds.putAll(this.ensemble.classify(essaySet, ensembleName, testLeaners,cv.getContext()));
		}
		
		// now prepare final result
		
		StrongLearner strong = this.ensemble.build(essaySet,ensembleName,learners);
		
		double trainingError = strong.getKappa();
		double cvError = Calc.kappa(essaySet, preds, groundTruth);
	//	Job.log(essaySet+"-"+ensembleName, "XVAL: training error = " + trainingError + " cv error = " + cvError);		
		
		strong.setKappa(cvError);
		return strong;
	}

	private WeakLearner find(String name, List<WeakLearner> learners) {
		for(WeakLearner learner : learners) {
			if(learner.getName().equals(name)) {
				return learner;
			}
		}
		return null;
	}
	
	@Override
	public Map<Double, Double> classify(int essaySet, String ensembleName,
			List<WeakLearner> learners,Object context) {
		return this.ensemble.classify(essaySet, ensembleName, learners,context);
	}

	
	
}
