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

package jjj.asap.sas.ga;

import java.util.UUID;

import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.SeededRandomGenerator;

public abstract class GAProgram {

	private int size;
	private Configuration config;
	private int seed;

	protected GAProgram(int size,int seed) {
		this.size = size;
		this.seed = seed;
	}
	
	protected GAProgram(int size) {
		this(size,1);
	}

	public void init() {

		try {

			// config
			String a_id = UUID.randomUUID().toString();
			Configuration.reset(a_id);
			config = new DefaultConfiguration(a_id,a_id);
			Configuration.reset(a_id);
			config.setRandomGenerator(new SeededRandomGenerator(seed));
			onInit(config);
			
			// fitness
			config.setFitnessFunction(getFitnessFunction());

			// model
			IChromosome chromosome = getSampleChromosome(config);
			config.setSampleChromosome(chromosome);

		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract FitnessFunction getFitnessFunction();
	
	//protected void onInit(Configuration config) throws Exception {
		//config.setPreservFittestIndividual(true); 
	//}
	protected abstract void onInit(Configuration config);

	protected abstract IChromosome getSampleChromosome(Configuration config) throws InvalidConfigurationException;

	final public Genotype evolve() {
		Genotype population = getPopulation(config);
		for(long k=1;;k++) {
			population.evolve();
			if(!onEvolve(population,k)) break;
		}
		return population;
	}

	protected abstract boolean onEvolve(Genotype population, long k);

	protected Genotype getPopulation(Configuration config) {
		try {
			config.setPopulationSize(size);
			return Genotype.randomInitialGenotype(config);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}

