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
import java.util.List;
import java.util.Map;

import jjj.asap.sas.ensemble.Ensemble;
import jjj.asap.sas.ensemble.StrongLearner;
import jjj.asap.sas.ensemble.WeakLearner;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.weka.DatasetBuilder;
import jjj.asap.sas.weka.Model;
import jjj.asap.sas.weka.Weka;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Implements stacking using labels only (that is, it doesn't consider
 * class support)
 */
public class StackedClassifier implements Ensemble {

	private boolean useNumericVariables;
	private Classifier prototype;

	public StackedClassifier(boolean useNumericVariables,Classifier prototype) {
		super();
		this.useNumericVariables = useNumericVariables;
		this.prototype = prototype;
	}

	@Override
	public StrongLearner build(int essaySet, String ensembleName,
			List<WeakLearner> learners) {

		if(learners.isEmpty()) {
			return StrongLearner.NO_MODEL[essaySet-1];
		}

		StrongLearner strong = new StrongLearner();

		// training
		try {

			Instances metaData = getMetaDataset(essaySet, learners);
			
			// hack
			//Instances hack = getMetaDataset(essaySet, learners);
			//hack.setRelationName("stacking"+essaySet);
			//Dataset.save("etc/stacking" + essaySet + ".arff", hack);
			// end hack
			
			Classifier metaClassifier = AbstractClassifier.makeCopy(prototype);

			Weka.trainClassifier(metaData,metaClassifier);
			Map<Double,double[]> probs = Weka.classifyInstances(metaData,metaClassifier);
			Map<Double,Double> preds = Model.getPredictions(essaySet, probs);
			double kappa = Calc.kappa(essaySet, preds, Contest.getGoldStandard(essaySet));

			strong.setKappa(kappa);
			strong.setPreds(preds);
			strong.setLearners(new ArrayList<WeakLearner>(learners));
			strong.setContext(metaClassifier);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return strong;
	}

	@Override
	public Map<Double, Double> classify(int essaySet, String ensembleName,
			List<WeakLearner> learners,Object context) {

		if(learners.isEmpty()) {
			return StrongLearner.NO_MODEL[essaySet-1].getPreds();
		}
		
		try {
			Map<Double,double[]> probs = Weka.classifyInstances(
					getMetaDataset(essaySet, learners), (Classifier) context);

			return Model.getPredictions(essaySet, probs);

		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a dataset representing the learners.
	 */
	private Instances getMetaDataset(int essaySet,List<WeakLearner> learners) {

		// create dataset headers

		DatasetBuilder builder = new DatasetBuilder();
		builder.addVariable("id");
		for(int i=0;i<learners.size();i++) {
			if(useNumericVariables) {
				builder.addVariable("x"+i);
			} else {
				builder.addNominalVariable("x"+i,Contest.getRubrics(essaySet));
			}
		}
		builder.addNominalVariable("score", Contest.getRubrics(essaySet));

		Instances dataset = builder.getDataset(this.getClass().getCanonicalName());
		Map<Double,Double> labels = Contest.getGoldStandard(essaySet);

		// now add the data
		for(double id : learners.get(0).getPreds().keySet()) {

			double[] data = new double[dataset.numAttributes()];
			data[0] = id;

			for(int i=0;i<learners.size();i++) {
				data[i+1] = learners.get(i).getPreds().get(id);
			}

			data[dataset.numAttributes()-1] = 
					labels.containsKey(id) ? labels.get(id) : Utils.missingValue();

					dataset.add(new DenseInstance(1.0,data));
		}

		return dataset;
	}


}
