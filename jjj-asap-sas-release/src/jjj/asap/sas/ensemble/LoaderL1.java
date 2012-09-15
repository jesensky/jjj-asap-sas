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

import java.util.Map;

import jjj.asap.sas.ensemble.WeakLearner.Loader;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.weka.Model;

public class LoaderL1 implements Loader {

	@Override
	public WeakLearner loadTraining(String name) {
		int k = Contest.getEssaySet(name);
		WeakLearner weak = new WeakLearner();
		weak.setName(name);
		Map<Double,double[]> probs = Model.loadProbabilities("work/models1/t/"+name);
		Map<Double,Double> preds = Model.getPredictions(k, probs);
		weak.setPreds(preds);
		weak.setProbs(probs);
		weak.setKappa(Calc.kappa(k, preds, Contest.getGoldStandard(0)));
		return weak;
	}

	@Override
	public WeakLearner loadTest(String name) {
		int k = Contest.getEssaySet(name);
		WeakLearner weak = new WeakLearner();
		weak.setName(name);
		Map<Double,double[]> probs = Model.loadCachedModel(name);
		Map<Double,Double> preds = Model.getPredictions(k, probs);
		weak.setPreds(preds);
		weak.setProbs(probs);
		weak.setKappa(0);
		return weak;
	}

}
