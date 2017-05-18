package edu.utexas.cs.nn.tasks.interactive.breedesizer;

import java.awt.image.BufferedImage;

import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.tasks.interactive.InteractiveEvolutionTask;
import edu.utexas.cs.nn.util.graphics.GraphicsUtil;
import edu.utexas.cs.nn.util.sound.MiscSoundUtil;
import edu.utexas.cs.nn.util.sound.SoundAmplitudeArrayManipulator;

public class BreedesizerTask<T extends Network> extends InteractiveEvolutionTask<T> {

	private static final int LENGTH_DEFAULT = 60000; //default length of generated amplitude
	private static final int FREQUENCY_DEFAULT = 440; //default frequency of generated amplitude: A440
	
	public static final int CPPN_NUM_INPUTS	= 3;
	public static final int CPPN_NUM_OUTPUTS = 1;

	public BreedesizerTask() throws IllegalAccessException {
		super();
		// TODO Auto-generated constructor stub
	}

	/* Will need method that plays sound when image is clicked. Can call utility methods
	 in edu.utexas.cs.nn.util.sounds to accomplish this, but will have to be differentiated 
	 from Picbreeder because it does not do this.
	 */

	/* After save and setEffectCheckbox are generalized so that they can be applied to both 
	 * Breedesizer and Picbreeder, specified method calls will have to be included here.
	 */

	@Override
	public String[] sensorLabels() {
		return new String[] { "X-coordinate", "Y-coordinate", "distance from center", "bias" }; //TODO: new strings here
	}

	@Override
	public String[] outputLabels() {
		return new String[] { "hue-value", "saturation-value", "brightness-value" }; //TODO: new strings here
	}

	@Override
	protected String getWindowTitle() {
		return "Breedesizer";
	}
	
	/**
	 * Creates BufferedImage from amplitude generated by network (saved in double array) and plays amplitude generated. 
	 */
	@Override
	protected BufferedImage getButtonImage(Network phenotype, int width, int height, double[] inputMultipliers) {
		double[] amplitude = SoundAmplitudeArrayManipulator.amplitudeGenerator(phenotype, LENGTH_DEFAULT, FREQUENCY_DEFAULT);
		BufferedImage wavePlotImage = GraphicsUtil.wavePlotFromDoubleArray(amplitude, height, width);
		
		// Move this
		//MiscSoundUtil.playDoubleArray(amplitude);

		return wavePlotImage;
	}

}
