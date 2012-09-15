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

package jjj.asap.sas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import weka.core.Utils;

/**
 * Misc. calculations
 */
public class Calc {

	/**
	 * Computes BWT of a string
	 */
	public static String bwt(final String text) {
		
		if(text == null || text.isEmpty()) {
			return StringUtils.safeForNLP(text);
		}
		
		final List<String> perms = new ArrayList<String>();
		perms.add(text);
		
		for(int i=1;i<text.length();i++) {
			perms.add(text.substring(i) + text.substring(0, i));
		}
		
		Collections.sort(perms);

		StringBuilder t =  new StringBuilder();
		for(String perm : perms) {
			t.append(perm.substring(perm.length()-1, perm.length()));
		}
		
		return t.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(bwt("x"));
		System.out.println(bwt("hello"));
		System.out.println(bwt("banana"));
		
	}
	
	/**
	 * @return the value clamped within the range
	 */
	public static double clamp(double x, double lb, double ub) {
		if(x > ub) return ub;
		if(x < lb) return lb;
		return x;
	}
	
	
	/**
	 * @return an average of kappa scores.
	 */
	public static double kappa(double[] x) {

		double[] z = new double[x.length];
		for(int i=0;i<x.length;i++) {
			z[i] = 0.5 * Math.log((1.0+x[i])/(1.0-x[i]));
		}
		double a = Utils.mean(z);
		double e = Math.exp(2.0*a);
		return (e-1)/(e+1);
	}
	
	/**
	 * Calculates the quadratic weighted kappa between two maps
	 * @param a the first map
	 * @param b the second map
	 * @return the quadratic weighted kappa score
	 */
	public static double kappa(int essaySet,Map<Double,Double> a, Map<Double,Double> b) {

		final int N = Contest.getRubrics(essaySet).size();

		// confusion matrix

		final int[][] confusion = new int[N][N];
		final int[] u = new int[N];
		final int[] v = new int[N];

		for(Double id : a.keySet()) {

			final int p = a.get(id).intValue();
			final int q = b.get(id).intValue();

			confusion[p][q]++;
			u[p]++;
			v[q]++;
			
		}

		// weights

		final double[][] w = new double[N][N];
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				w[i][j] = (double)((i-j)*(i-j))/((N-1)*(N-1));
			}
		}

		// finally, calculate kappa
		// k = 1 - o/e;

		double o=0,e=0;
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				o += w[i][j] * (double)confusion[i][j] / (double)a.size();
				e += w[i][j] * ((double)u[i]*(double)v[j]/(double)a.size()) / (double)a.size();
			}
		}

		return 1.0 - o/e;
	}

	public static double[] add1(double[] a, double[] b) {
		if(a.length != b.length) {
			throw new IllegalArgumentException(a.length + " != " + b.length);
		}
		double[] s = new double[a.length];
		for(int i=0;i<a.length;i++) {
			s[i] = (a[i] + b[i])/2.0;
		}
		return s;
	}

	public static int argmax(double[] x) {
		double maxVal = -99999.99999;
		int maxIdx = -1;
		
		for(int i=0;i<x.length;i++) {
			if(x[i]>maxVal) {
				maxVal = x[i];
				maxIdx = i;
			}
		}
		
		return maxIdx;
	}
}
