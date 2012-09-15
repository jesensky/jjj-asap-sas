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

package jjj.asap.sas.models3.job;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.IOUtils;
import jjj.asap.sas.util.Job;
import jjj.asap.sas.weka.Model;

/**
 * Assembles the models (predictions) for each individual essay set
 * into one CSV file and calcuates the average kappa. 
 */
public class Assemble extends Job {

	/**
	 * Usage: Either:
	 * 
	 * args = [ outputTag, inputTag ] 
	 * 
	 * or
	 * 
	 * args = [ outputTag, model#1, model#2, ..., model#10 ]
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Job job = new Assemble(args);
		Job.log("ARGS",Arrays.toString(args));		
		job.start();

	}

	private String[] inputs;
	private String output;

	public Assemble(String[] args) {
		super();
		this.inputs = new String[10];
		if(args.length == 2) {
			this.output = args[0];
			for(int i=1;i<=10;i++) {
				this.inputs[i-1] = i + "-" + args[1];
			}
		} else if(args.length == 11) {
			this.output = args[0];
			for(int i=1;i<=10;i++) {
				this.inputs[i-1] = args[i];
			}
		} else {
			throw new IllegalArgumentException(Arrays.toString(args));
		}
	} 

	@Override
	protected void run() throws Exception {

		// init I/O
		
		final String outputFile = "work/models3/" + this.output + ".csv";
		final String manifestFile = "work/models3/" + this.output + ".manifest";
		
		if(IOUtils.exists(outputFile) && IOUtils.exists(manifestFile)) {
			Job.log("NOTE",outputFile + " already exists - nothing to do.");
			Job.log("NOTE",manifestFile + " already exists - nothing to do.");
			return;
		}
		
		//  init
		
		PrintWriter writer = new PrintWriter(manifestFile);
		double kappas[] = new double[10];
		Map<Double,Double> preds = new TreeMap<Double,Double>();
		
		// do work
		
		for(int k=0;k<10;k++) {
			
			final int essaySet = k+1;
			final String inputFile = this.inputs[k] + ".csv";
			
			// get the cross-validated predictions
			Map<Double,Double> cv = Model.loadPredictions("work/models2/t/"+inputFile);
			kappas[k] = Calc.kappa(essaySet, cv, Contest.getGoldStandard(essaySet));
			
			// get the predictions of unseen examples
			Map<Double,Double> labels = Model.loadPredictions("work/models2/u/"+inputFile);
			
			// update
			writer.println(essaySet + "\t" + kappas[k] + "\t" + inputFile);
			preds.putAll(labels);
		}
		
		// wrap up
		
		Model.savePredictions(outputFile, preds);
		
		double kappa = Calc.kappa(kappas);
		writer.println("kappa = " + kappa);
		writer.flush();
		writer.close();
		
	}

}
