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

package jjj.asap.sas.ensemble.scheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jjj.asap.sas.ensemble.Ensemble;
import jjj.asap.sas.ensemble.Scheme;
import jjj.asap.sas.ensemble.StrongLearner;
import jjj.asap.sas.ensemble.WeakLearner;
import jjj.asap.sas.ga.GAProgram;
import jjj.asap.sas.util.Calc;
import jjj.asap.sas.util.Contest;
import jjj.asap.sas.util.Mask;
import jjj.asap.sas.util.Progress;
import jjj.asap.sas.weka.Model;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.BooleanGene;

import weka.core.Utils;

/**
 * Uses a genetic algorithm to do model selection.
 */
public class GACommittee implements Scheme {

	private int populationSize;
	private int numGenerations;
	private Ensemble ensemble;
	private int committeeSize;

	public GACommittee(int populationSize, int numGenerations,
			Ensemble ensemble, int committeeSize) {
		this.populationSize = populationSize;
		this.numGenerations = numGenerations;
		this.ensemble = ensemble;
		this.committeeSize = committeeSize;
	}


	@Override
	public StrongLearner build(int essaySet, String ensembleName,
			List<WeakLearner> learners) {

		if(learners.isEmpty()) {
			return StrongLearner.NO_MODEL[essaySet-1];
		}

		StrongLearner[] experts = new StrongLearner[this.committeeSize];
		Mask[] solutions = new Mask[this.committeeSize];

		//Progress progress = new Progress(this.committeeSize,essaySet+"-"+ensembleName);
		for(int i=0;i<this.committeeSize;i++) {

			// create the GA model selector
			GAModelSelection ga = new GAModelSelection(populationSize,numGenerations,
					ensemble,essaySet,ensembleName,learners,i+1);

			// run it
			ga.init();
			Genotype population = ga.evolve();

			// re-construct b

			Gene[] genes = population.getFittestChromosome().getGenes();
			Mask solution = new Mask(genes.length);
			List<WeakLearner> selected = new ArrayList<WeakLearner>();
			for(int j=0;j<genes.length;j++) {
				BooleanGene gene = (BooleanGene)genes[j];
				if(gene.booleanValue()) {
					selected.add(learners.get(j));
					solution.getMask()[j] = true;
				}
			}

			experts[i] = this.ensemble.build(essaySet, ensembleName, selected);
			solutions[i] = solution;

			//progress.tick();
		}
		//progress.done();

		// build final model

		// final model is majority vote over experts 
		StrongLearner committee = new StrongLearner();

		// include all models
		committee.setLearners(learners);

		// contexts is an array of all the context objects
		Context[] contexts = new Context[this.committeeSize];
		for(int i=0;i<contexts.length;i++) {
			contexts[i] = new Context();
			contexts[i].context = experts[i].getContext();
			contexts[i].mask = solutions[i];
		}
		committee.setContext(contexts);

		// what are the predictions by committee
		// we can treat the net of the votes like class probs
		int numClasses = Contest.getRubrics(essaySet).size();
		Map<Double,double[]> probs = new HashMap<Double,double[]>();
		for(double id : experts[0].getPreds().keySet()) {
			double[] prob = new double[numClasses];
			for(int i=0;i<experts.length;i++) {
				int vote = experts[i].getPreds().get(id).intValue();
				prob[vote]++;
			}
			Utils.normalize(prob);
			probs.put(id,prob);
		}

		committee.setPreds(Model.getPredictions(essaySet, probs));
		committee.setKappa(Calc.kappa(essaySet, committee.getPreds(), Contest.getGoldStandard(essaySet)));

		return committee;
	}

	@Override
	public Map<Double, Double> classify(int essaySet, String ensembleName,
			List<WeakLearner> learners,Object context) {

		if(learners.isEmpty()) {
			return StrongLearner.NO_MODEL[essaySet-1].getPreds();
		}

		// get votes
		Context[] contexts = (Context[])context;
		Map[] votes = new Map[this.committeeSize];
		for(int i=0;i<this.committeeSize;i++) {

			// mask
			List<WeakLearner> selectedLearners = new ArrayList<WeakLearner>();
			Mask solution = contexts[i].mask;
			for(int j=0;j<solution.getMask().length;j++) {
				if(solution.getMask()[j]) {
					selectedLearners.add(learners.get(j));
				}
			}

			// classify
			votes[i] = this.ensemble.classify(essaySet, ensembleName, selectedLearners,contexts[i].context);
		}

		// combine
		Map<Double,Double> preds = new HashMap<Double,Double>();

		// what are the predictions by committee
		// we can treat the net of the votes like class probs
		int numClasses = Contest.getRubrics(essaySet).size();
		Map<Double,double[]> probs = new HashMap<Double,double[]>();
		for(double id : learners.get(0).getPreds().keySet()) {
			double[] prob = new double[numClasses];
			for(int i=0;i<votes.length;i++) {
				int vote = ((Map<Double,Double>)votes[i]).get(id).intValue();
				prob[vote]++;
			}
			Utils.normalize(prob);
			probs.put(id,prob);
		}

		return Model.getPredictions(essaySet, probs);		
	}

	private static class GAModelSelection extends GAProgram {

		private int populationSize;
		private int numGenerations;
		private Ensemble ensemble;
		private int essaySet;
		private String ensembleName;
		private List<WeakLearner> learners;
		private KappaFunction kappaFunction;

		private double bestKappa = -99999.99999;

		public GAModelSelection(int populationSize, int numGenerations,
				Ensemble ensemble, int essaySet, String ensembleName,
				List<WeakLearner> learners, int seed) {

			super(populationSize,seed);

			this.populationSize = populationSize;
			this.numGenerations = numGenerations;
			this.ensemble = ensemble;
			this.essaySet = essaySet;
			this.ensembleName = ensembleName;
			this.learners = learners;

			// fitness function
			this.kappaFunction = new KappaFunction(
					essaySet,ensembleName,ensemble,learners);
		}	

		@Override
		protected FitnessFunction getFitnessFunction() {
			return this.kappaFunction;
		}

		/**
		 * Set any custom configuration here
		 */
		@Override
		protected void onInit(Configuration config) {
			config.setPreservFittestIndividual(true);
		}

		/**
		 * Need to return the prototype chromosome
		 */
		@Override
		protected IChromosome getSampleChromosome(Configuration config)
				throws InvalidConfigurationException {

			Gene[] genes = new BooleanGene[this.learners.size()];
			for(int i=0;i<genes.length;i++) {
				genes[i] = new BooleanGene(config);
			}
			return new Chromosome(config,genes);

		}

		/**
		 * Called every generation
		 * @return false to stop evolving
		 */
		@Override
		protected boolean onEvolve(Genotype population, long k) {

			double kappa = population.getFittestChromosome().getFitnessValueDirectly();
			if(kappa > bestKappa) {
				bestKappa = kappa;
				//Job.log(essaySet+"-"+ensembleName,"GA: kappa = " + bestKappa+" pop = " + population.getPopulation().size() +" gen = "+k);
			}

			return k<this.numGenerations;
		}

	}

	/**
	 * The fitness function
	 */
	private static class KappaFunction extends FitnessFunction {

		private int essaySet;
		private String ensembleName;
		private Ensemble ensemble;
		private List<WeakLearner> learners;

		private StrongLearner best;

		private Map<Mask,Double> cache;

		public KappaFunction(int essaySet, String ensembleName, Ensemble ensemble,
				List<WeakLearner> learners) {
			this.essaySet = essaySet;
			this.ensembleName = ensembleName;
			this.ensemble = ensemble;
			this.learners = learners;
			this.cache = new HashMap<Mask,Double>();
		}

		@Override
		protected double evaluate(IChromosome chromosome) {

			//
			// From the chromosome, figure out which models to select.
			//

			List<WeakLearner> selected = new ArrayList<WeakLearner>();
			Gene[] genes = chromosome.getGenes();
			Mask key = new Mask(genes.length);
			for(int i=0;i<genes.length;i++) {
				BooleanGene gene = (BooleanGene)genes[i];
				if(gene.booleanValue()) {
					selected.add(learners.get(i));
					key.getMask()[i] = true;
				}
			} 

			// calc fitness

			Double kappa = cache.get(key);
			if(kappa != null) {
				return kappa;
			} else {
				StrongLearner phenotype = this.ensemble.build(essaySet, ensembleName, selected);
				kappa = phenotype.getKappa();
				cache.put(key, kappa);
				return kappa;
			}
		}

	}

	private static class Context {

		public Mask mask;
		public Object context;

	}

}
