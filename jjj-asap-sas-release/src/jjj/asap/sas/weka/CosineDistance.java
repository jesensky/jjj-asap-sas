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
 * CosineDistance.java adapted from Katoa (http://www.cs.waikato.ac.nz/~lh92/katoa/ ).
 * Contact the original author(s) for copyright status.
 */

package jjj.asap.sas.weka;

import java.io.Serializable;
import java.util.Enumeration;

import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 * Implements the cosine similarity measure as a distance function.
 * 
 * @author Anna Huang
 * @author jesensky
 */
public class CosineDistance implements DistanceFunction, Serializable {

	/** for serialization */
	private static final long serialVersionUID = 4733687900826669897L;

	/** maximum distance value */
	private final double m_max = 1.0;

	/** minimum distance value */
	private final double m_min = 0.0;

	/** index of the class attribute */
	private int classIndex = -1;

	@Override
	public void setInstances(Instances instances) {
		for (int i = 0; i < instances.numAttributes(); i++) {
			if (instances.attribute(i).isNumeric() == false && instances.classIndex() != i) {
				System.err.println("Can't handle non-numeric attributes");
			}
		}
		classIndex = instances.classIndex();
	}

	@Override
	public double distance(Instance first, Instance second) {

		double similarity = 0.0, product = 0.0, lengthA = 0.0, lengthB = 0.0, value = 0.0;
		int idx = 0;

		for (int v = 0; v < first.numValues(); v++) {
			idx = first.index(v);
			if (idx == classIndex)
				continue;

			value = first.valueSparse(v);
			product += value * second.value(idx);
			lengthA += value * value;
		}

		for (int v = 0; v < second.numValues(); v++) {
			idx = second.index(v);

			if (idx == classIndex)
				continue;

			value = second.valueSparse(v);
			lengthB += value * value;
		}

		lengthA = Math.sqrt(lengthA);
		lengthB = Math.sqrt(lengthB);

		// empty instances
		if (lengthA == 0 || lengthB == 0) {
			return m_max;
		}

		similarity = product / (lengthA * lengthB);

		if (Double.isNaN(similarity)) {
			System.err.println("similarity is NaN, product = " + product
					+ ", lengthA = " + lengthA + ", lengthB = " + lengthB
					+ "\nfirst: " + first + "\nsecond: " + second);
			return m_max;
		}
		if (Double.isInfinite(similarity)) {
			System.err.println("similarity is Infinite");
			return m_min;
		}

		return m_max - similarity;
	}

	@Override
	public double distance(Instance arg0, Instance arg1, PerformanceStats arg2)
			throws Exception {
		return distance(arg0, arg1);
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2) {
		return distance(arg0, arg1);
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2,
			PerformanceStats arg3) {
		return distance(arg0, arg1);
	}

	@Override
	public String[] getOptions() {
		return new String[0];
	}

	@Override
	public Enumeration listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAttributeIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instances getInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getInvertSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postProcessDistances(double[] arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttributeIndices(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInvertSelection(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Instance arg0) {
		// TODO Auto-generated method stub

	}

}
