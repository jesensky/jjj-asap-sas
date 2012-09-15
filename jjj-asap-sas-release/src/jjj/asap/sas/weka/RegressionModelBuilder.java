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

package jjj.asap.sas.weka;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.naming.OperationNotSupportedException;

import jjj.asap.sas.util.Bucket;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.util.Timer;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Builds a level 1 model using a regression algorithm.
 */
public class RegressionModelBuilder implements Callable<Object> {

	private String dsn;
	private String tag; 
	private Classifier prototype;
	private String outputBucket;
	

	public RegressionModelBuilder(String dsn, String tag, Classifier prototype) {
		throw new RuntimeException(new OperationNotSupportedException());
	}
	
	public RegressionModelBuilder(String dsn, String tag, Classifier prototype, String outputBucket) {
		super();
		this.dsn = dsn;
		this.tag = tag;
		this.prototype = prototype;
		this.outputBucket = outputBucket;
	}

	@Override
	public Object call() throws Exception {

		// files
		String $input = "work/datasets/t/" + dsn;
		String $output = "work/models1/t/" + dsn + "@" + tag + "@probs";
		String $model = "work/models1/t/" + dsn + "@" + tag + "@model";
		String $residuals = "work/models1/t/" + dsn + "@" + tag + "@res"; // residuals
		
		// verify work needs done
		if(IOUtils.exists($output) && IOUtils.exists($model) && IOUtils.exists($residuals)) {
			Job.log("NOTE",$output + " already exists - nothing to do.");
			Job.log("NOTE",$model + " already exists - nothing to do.");
			Job.log("NOTE",$residuals + " already exists - nothing to do.");
			return null;
		}

		// note in log
		Job.logWekaObject(prototype);

		// load dataset
		Instances dataset = Dataset.load($input);
		int essaySet = Contest.getEssaySet(dsn);

		// cross validate
		Timer timer = new Timer();
		Map<Double,Double> res = Weka.regCrossValidate(dataset, prototype, 10,1);
		Job.log(tag, "Cross validation took " + timer.getElapsedSeconds() + " secs.");
		
		Map<Double,Double> preds = Model.getPredictionsFromResiduals(essaySet, res);
		Map<Double,double[]> probs = Model.getProbabilties(essaySet, preds);
		
		Model.saveProbabilities($output,probs);
		Model.savePredictions($residuals,res);	// predictions are just a double->double map
		
		// save classifier
		Classifier model = AbstractClassifier.makeCopy(prototype);
		SerializationHelper.write($model, model);

		// put in bucket
		
		double kappa = Calc.kappa(
				essaySet,
				Model.getPredictions(essaySet, probs),
				Contest.getGoldStandard(essaySet));
		Bucket.add(
				"models",
				this.outputBucket,
				IOUtils.getName($output),
				String.valueOf(kappa));
		
		return null;
	}

}
