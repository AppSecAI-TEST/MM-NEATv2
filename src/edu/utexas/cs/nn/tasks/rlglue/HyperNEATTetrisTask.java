package edu.utexas.cs.nn.tasks.rlglue;

import java.util.LinkedList;
import java.util.List;

import org.rlcommunity.environments.tetris.TetrisState;

import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.networks.hyperneat.HyperNEATTask;
import edu.utexas.cs.nn.networks.hyperneat.Substrate;
import edu.utexas.cs.nn.util.datastructures.Pair;
import edu.utexas.cs.nn.util.datastructures.Triple;

public class HyperNEATTetrisTask extends RLGlueTask<Network>implements HyperNEATTask {

	// These values will be defined before they are needed
	public static final int HYPERNEAT_OUTPUT_SUBSTRATE_DIMENSION = 1;
	private static final int SUBSTRATE_COORDINATES = 4;
	@SuppressWarnings("unused")
	private static int numSubstrateInputs;
	private static List<Substrate> substrateInformation = null;
	private List<Pair<String, String>> substrateConnectivity = null;

	@Override
	public List<Substrate> getSubstrateInformation() {
		if (substrateInformation == null) {
			substrateInformation = new LinkedList<Substrate>();
			int worldWidth = TetrisState.worldWidth;
			int worldHeight = TetrisState.worldHeight;
			numSubstrateInputs = 0;
			numSubstrateInputs = worldWidth * worldHeight;

			Triple<Integer, Integer, Integer> inSubCoord = new Triple<Integer, Integer, Integer>(0, 0, 0);
			Triple<Integer, Integer, Integer> processSubCoord = new Triple<Integer, Integer, Integer>(0,
					SUBSTRATE_COORDINATES, 0);
			Triple<Integer, Integer, Integer> outSubCoord = new Triple<Integer, Integer, Integer>(0,
					SUBSTRATE_COORDINATES * 2, 0);

			Pair<Integer, Integer> substrateDimension = new Pair<Integer, Integer>(worldWidth, worldHeight);
			Pair<Integer, Integer> outputSubstrateDimension = new Pair<Integer, Integer>(
					HYPERNEAT_OUTPUT_SUBSTRATE_DIMENSION, HYPERNEAT_OUTPUT_SUBSTRATE_DIMENSION);

			Substrate inputSub = new Substrate(substrateDimension, Substrate.INPUT_SUBSTRATE, inSubCoord, "input_0");
			Substrate processSub = new Substrate(substrateDimension, Substrate.PROCCESS_SUBSTRATE, processSubCoord,
					"process_0");
			Substrate outputSub = new Substrate(outputSubstrateDimension, Substrate.OUTPUT_SUBSTRATE, outSubCoord,
					"output_0");

			substrateInformation.add(inputSub);
			substrateInformation.add(processSub);
			substrateInformation.add(outputSub);
		}

		return substrateInformation;
	}

	/**
	 * Connects substrates fully, in basic configuration
	 */
	@Override
	public List<Pair<String, String>> getSubstrateConnectivity() {
		if (substrateConnectivity == null) {
			substrateConnectivity = new LinkedList<Pair<String, String>>();
			substrateConnectivity.add(new Pair<String, String>("input_0", "process_0"));
			substrateConnectivity.add(new Pair<String, String>("process_0", "output_0"));
		}
		return substrateConnectivity;
	}

	/**
	 * This class no longer necessary. Instead, use the extract method from the
	 * HyperNEATTetris extractor
	 */
	@Override
	public double[] getSubstrateInputs(List<Substrate> inputSubstrates) {
		throw new UnsupportedOperationException("Substrate inputs generated by hyperNEAT extractor, not this method!");
	}

}