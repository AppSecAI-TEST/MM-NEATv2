/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utexas.cs.nn.tasks.testmatch;

import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.networks.NetworkTask;
import edu.utexas.cs.nn.parameters.CommonConstants;
import edu.utexas.cs.nn.scores.Score;
import edu.utexas.cs.nn.tasks.LonerTask;
import edu.utexas.cs.nn.util.MiscUtil;
import edu.utexas.cs.nn.util.datastructures.Pair;
import edu.utexas.cs.nn.util.stats.StatisticsUtilities;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class takes input and output pairs that a network needs to evolve to match
 * returns a fitness value based on the Mean Squared Error 
 * (how far the actual outputs are from the desired outputs)
 * @author Jacob Schrum
 * @param <T> Phenotype being evolved, which must be a network
 */
public abstract class MatchDataTask<T extends Network> extends LonerTask<T> implements NetworkTask {
	
	public static boolean pauseForEachCase = true;
	/**
	 * create a matchDataTask object with default values for the fitness
	 */
	public MatchDataTask(){
		MMNEAT.registerFitnessFunction("Error", null, true);
	}

	@Override
	/**
	 * a method that evaluated a given individual based on its genotype by
	 * providing a score (or fitness level) 
	 * @param individual a genotype of a given agent
	 * @return the score of the individual
	 */
	public Score<T> evaluate(Genotype<T> individual) {
		//RandomNumbers.randomGenerator = new Random(0);
		ArrayList<Pair<double[],double[]>> trainingSet = getTrainingPairs();
		ArrayList<ArrayList<Pair<Double,Double>>> samples = new ArrayList<ArrayList<Pair<Double,Double>>>(trainingSet.size());
		Network n = individual.getPhenotype();
		//loop that runs for each "pattern" in the trainingSet, which is a pair of double arrays of inputs/outputs
		for(Pair<double[],double[]> pattern : trainingSet){
			double[] inputs = pattern.t1;
			double[] desiredOutputs = pattern.t2;
			//find the actual outputs based on the given inputs
			double[] actualOutputs = n.process(inputs);
			if(CommonConstants.watch) {
				System.out.println("Desired: "+ Arrays.toString(desiredOutputs) + ", Actual: " + Arrays.toString(actualOutputs));
			}
			ArrayList<Pair<Double,Double>> neuronResults = new ArrayList<Pair<Double,Double>>(n.numOutputs());
			for(int i = 0; i < desiredOutputs.length; i++){
				//add all of the pairs of desired and actual outputs to compare
                                assert !Double.isNaN(desiredOutputs[i]) : "desiredOutputs["+i+"] is NaN!";
                                assert !Double.isNaN(actualOutputs[i]) : "actualOutputs["+i+"] is NaN!";
				neuronResults.add(new Pair<Double,Double>(desiredOutputs[i], actualOutputs[i]));
			}
			//add all of the pairs of desired and actual outputs to compare
			samples.add(neuronResults);
			if(CommonConstants.watch && pauseForEachCase) {
				MiscUtil.waitForReadStringAndEnterKeyPress();
			}
		}
		//find the average amount/occurrence of error between each desired and actual output pairs put into the samples
		double averageError = StatisticsUtilities.averageSquaredErrorEnergy(samples);
                assert !Double.isNaN(averageError) : "averageError is NaN!";
		return new Score<T>(individual, new double[]{-averageError}, null); // minimize error, so score is negative error
	}

	/**
	 * Finds the number of inputs for the pair for the network to evolve and match
	 * @return the number of inputs
	 */
	public abstract int numInputs();

	/**
	 * Finds the number of outputs for the pair for the network to evolve and match
	 * @return the number of outputs
	 */
	public abstract int numOutputs();

	/**
	 * Just the approximation error
	 * @return 
	 */
	public int numObjectives() {
		return 1;
	}

	/**
	 * returns the timeStamp, which is 0 by default
	 */
	public double getTimeStamp() {
		return 0;
	}

	/**
	 * Get or generate a collection of desired input/output pairs to train on.
	 * @return collection of Pairs
	 */
	public abstract ArrayList<Pair<double[],double[]>> getTrainingPairs();


}
