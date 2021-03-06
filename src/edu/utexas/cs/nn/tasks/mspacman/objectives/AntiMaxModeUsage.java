/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utexas.cs.nn.tasks.mspacman.objectives;

import edu.utexas.cs.nn.evolution.Organism;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype;
import edu.utexas.cs.nn.evolution.metaheuristics.AntiMaxModuleUsageFitness;
import edu.utexas.cs.nn.networks.Network;

/**
 *
 * @author Jacob Schrum
 */
public class AntiMaxModeUsage<T extends Network> extends MsPacManObjective<T> {

	private final AntiMaxModuleUsageFitness meta;

	public AntiMaxModeUsage() {
		this.meta = new AntiMaxModuleUsageFitness();
	}

	@Override
	public double minScore() {
		return meta.minScore();
	}

	public double fitness(Organism<T> individual) {
		return meta.getScore((TWEANNGenotype) individual.getGenotype());
	}
}
