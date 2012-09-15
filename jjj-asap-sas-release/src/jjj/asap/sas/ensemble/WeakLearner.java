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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single learner's outputs
 */
public class WeakLearner implements Comparable<WeakLearner> {

	private String name;
	private Map<Double,Double> preds;
	private Map<Double,double[]> probs;
	private double kappa;
	
	public WeakLearner copyOf() {
		WeakLearner copy = new WeakLearner();
		copy.kappa = kappa;
		copy.name = name;
		copy.getPreds().putAll(preds);
		copy.getProbs().putAll(probs);
		return copy;
	}
	
	public WeakLearner() {
		super();
		this.preds = new HashMap<Double,Double>();
		this.probs = new HashMap<Double,double[]>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the probs
	 */
	public Map<Double, double[]> getProbs() {
		return probs;
	}

	/**
	 * @param probs the probs to set
	 */
	public void setProbs(Map<Double, double[]> probs) {
		this.probs = probs;
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
	 * Something that can load weak leaners
	 */
	public static interface Loader {
		public WeakLearner loadTraining(final String name);
		public WeakLearner loadTest(final String name);
	}

	public WeakLearner deepCopyOf() {
		WeakLearner copy = new WeakLearner();
		copy.kappa = kappa;
		copy.name = name;
		copy.getPreds().putAll(preds);
		for(Map.Entry<Double, double[]> prob : probs.entrySet()) {
			copy.getProbs().put(prob.getKey(),prob.getValue().clone());
		}
		return copy;
	}

	/**
	 * In this case, the best (highest) kappa will be first.
	 */
	@Override
	public int compareTo(WeakLearner arg0) {
		return (int) Math.signum(arg0.kappa - this.kappa);
	}
}
