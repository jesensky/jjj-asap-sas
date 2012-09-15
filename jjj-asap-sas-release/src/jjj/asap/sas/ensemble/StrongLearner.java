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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jjj.asap.sas.text.job.Unmarshall;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;

/**
 * Represent the combined predictions of several weak leanrers
 */
public class StrongLearner  {

	private Map<Double,Double> preds;
	private double kappa;
	private List<WeakLearner> learners;
	private Object context;
	
	public StrongLearner() {
		super();
		this.preds = new HashMap<Double,Double>();
		this.learners = new ArrayList<WeakLearner>();
	}

	/**
	 * @return a "mostly deep" copy of the object
	 */
	public StrongLearner copyOf() {
		StrongLearner copy = new StrongLearner();
		
		copy.setKappa(kappa);
		copy.setContext(context);
		copy.getPreds().putAll(preds);
		copy.getLearners().addAll(learners);
		
		return copy;
	}
	
	
	/**
	 * @return the learners
	 */
	public List<WeakLearner> getLearners() {
		return learners;
	}

	/**
	 * @param learners the learners to set
	 */
	public void setLearners(List<WeakLearner> learners) {
		this.learners = learners;
	}

	/**
	 * @return the preds
	 */
	public Map<Double, Double> getPreds() {
		return preds;
	}

	/**
	 * @param preds the preds to set
	 */
	public void setPreds(Map<Double, Double> preds) {
		this.preds = preds;
	}

	/**
	 * @return the kappa
	 */
	public double getKappa() {
		return kappa;
	}

	/**
	 * @param kappa the kappa to set
	 */
	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	/**
	 * In case no weak learners, you can use this
	 */
	public static final StrongLearner[] NO_MODEL = new StrongLearner[10];
	static {
		for(int k=0;k<10;k++) {
			int essaySet = k + 1;
			double mode = Contest.getPriorRank(essaySet)[0];
			NO_MODEL[k] = new StrongLearner();
			Map<Double,Double> groundTruth = Contest.getGoldStandard(essaySet);
			for(double id : groundTruth.keySet()) {
				if(!Unmarshall.REJECTS.contains(id)) {
					NO_MODEL[k].getPreds().put(id, mode);
				}
			}
			NO_MODEL[k].setKappa(Calc.kappa(essaySet, NO_MODEL[k].getPreds(), groundTruth));
		}
	}
	
	/**
	 * @return the context
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Object context) {
		this.context = context;
	}
	
	
	
}
