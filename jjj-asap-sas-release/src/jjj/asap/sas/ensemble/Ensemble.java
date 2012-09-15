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

import java.util.List;
import java.util.Map;

/**
 * An object that can train an ensemble of models and then make predictions
 * using that combined model.
 */
public interface Ensemble {

	/**
	 * Builds an ensemble classifier from a list of weak learners.
	 * @param learners
	 * @return
	 */
	public StrongLearner build(int essaySet,String ensembleName,List<WeakLearner> learners);
	
	/**
	 * Classifies 
	 * @param leaners the weak learners' outcomes on unlabeled data
	 * @return
	 */
	public Map<Double,Double> classify(int essaySet,String ensembleName,List<WeakLearner> learners,Object context);

}
