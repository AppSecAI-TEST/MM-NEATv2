package edu.utexas.cs.nn.util.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import edu.utexas.cs.nn.networks.ActivationFunctions;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.tasks.interactive.picbreeder.PicbreederTask;
import edu.utexas.cs.nn.util.CartesianGeometricUtilities;
import edu.utexas.cs.nn.util.datastructures.ArrayUtil;
import edu.utexas.cs.nn.util.sound.SoundToArray;
import edu.utexas.cs.nn.util.util2D.ILocated2D;
import edu.utexas.cs.nn.util.util2D.Tuple2D;

/**
 * Several useful methods for creating and manipulating images.
 * Mostly used by Picbreeder and PictureRemix.
 * 
 * @author Lauren Gillespie, edits by Isabel Tweraser
 *
 */
public class GraphicsUtil {

	private static final int HUE_INDEX = 0;
	private static final int SATURATION_INDEX = 1;
	private static final int BRIGHTNESS_INDEX = 2;
	private static final double BIAS = 1.0;// a common input used in neural networks
	private static final double SQRT2 = Math.sqrt(2); // Used for scaling distance from center
	
	public static BufferedImage imageFromCPPN(Network n, int imageWidth, int imageHeight) {
		return imageFromCPPN(n,imageWidth,imageHeight, ArrayUtil.doubleOnes(PicbreederTask.CPPN_NUM_INPUTS));
	}
	
	/**
	 * Draws the image created by the CPPN to a BufferedImage
	 *
	 * @param n
	 *            the network used to process the image
	 * @param imageWidth
	 *            width of image
	 * @param imageHeight
	 *            height of image
	 * @return buffered image containing image drawn by network
	 */
	public static BufferedImage imageFromCPPN(Network n, int imageWidth, int imageHeight, double[] inputMultiples) {
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < imageWidth; x++) {// scans across whole image
			for (int y = 0; y < imageHeight; y++) {
				float[] hsb = getHSBFromCPPN(n, x, y, imageWidth, imageHeight, inputMultiples);
				// network outputs computed on hsb, not rgb scale because
				// creates better images
				Color childColor = Color.getHSBColor(hsb[HUE_INDEX], hsb[SATURATION_INDEX], hsb[BRIGHTNESS_INDEX]);
				// set back to RGB to draw picture to JFrame
				image.setRGB(x, y, childColor.getRGB());
			}
		}
		return image;
	}
	
	/**
	 * Returns adjusted image based on manipulation of an input image with a CPPN.
	 * 
	 * @param n CPPN
	 * @param img input image being "remixed"
	 * @param inputMultiples array of multiples indicating wheter to turn activation functions on or off
	 * @return BufferedImage representation of adjusted image
	 */
	public static BufferedImage remixedImageFromCPPN(Network n, BufferedImage img, double[] inputMultiples) {
		//initialize new image
		BufferedImage remixedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				//get HSB from input image
				float[] originalHSB = getHSB(img, x, y); //not sure how this is used
				ILocated2D scaled = CartesianGeometricUtilities.centerAndScale(new Tuple2D(x, y), img.getWidth(), img.getHeight());
				double[] remixedInputs = { scaled.getX(), scaled.getY(), scaled.distance(new Tuple2D(0, 0)) * SQRT2, originalHSB[0], originalHSB[1], originalHSB[2], BIAS };
				// Multiplies the inputs of the pictures by the inputMultiples; used to turn on or off the effects in each picture
				for(int i = 0; i < inputMultiples.length; i++) {
					remixedInputs[i] = remixedInputs[i] * inputMultiples[i];
				}			
				n.flush(); // erase recurrent activation
				float[] hsb = rangeRestrictHSB(n.process(remixedInputs));
				Color childColor = Color.getHSBColor(hsb[HUE_INDEX], hsb[SATURATION_INDEX], hsb[BRIGHTNESS_INDEX]);
				// set back to RGB to draw picture to JFrame
				remixedImage.setRGB(x, y, childColor.getRGB());
			}
		}
		return remixedImage;
	}
	
	/**
	 * Accesses HSB at a specific pixel in a BufferedImage. Does so by accessing the 
	 * RGB first and then creating a Color class instance to convert the components of 
	 * the RGB to HSB. Creates an array of floats that numerically represent the hue,
	 * saturation, and brightness of the pixel. 
	 * 
	 * @param img Image containing pixel
	 * @param x x-coordinate of pixel
	 * @param y y-coordinate of pixel
	 * @return array of floats representing hue, saturation, and brightness of pixel
	 */
	private static float[] getHSB(BufferedImage img, int x, int y) {
		int RGB = img.getRGB(x, y);
		 Color c = new Color(RGB, true);
		 int r = c.getRed();
		 int g = c.getGreen();
		 int b = c.getBlue();
		 float[] HSB = Color.RGBtoHSB(r, g, b, null);
		 return HSB;
	}

	/**
	 * Gets HSB outputs from the CPPN in question
	 *
	 * @param n
	 *            the CPPN
	 * @param x
	 *            x-coordinate of pixel
	 * @param y
	 *            y-coordinate of pixel
	 * @param imageWidth
	 *            width of image
	 * @param imageHeight
	 *            height of image
	 *
	 * @return double containing the HSB values
	 */
	public static float[] getHSBFromCPPN(Network n, int x, int y, int imageWidth, int imageHeight, double[] inputMultiples) {

		double[] input = getCPPNInputs(x, y, imageWidth, imageHeight);

		// Multiplies the inputs of the pictures by the inputMultiples; used to turn on or off the effects in each picture
		for(int i = 0; i < inputMultiples.length; i++) {
			input[i] = input[i] * inputMultiples[i];
		}
		
		// Eliminate recurrent activation for consistent images at all resolutions
		n.flush();
		return rangeRestrictHSB(n.process(input));
	}
	
	/**
	 * Given the direct HSB values from the CPPN (a double array), convert to a
	 * float array (required by Color methods) and do range restriction on
	 * certain values.
	 * 
	 * These range restrictions were stolen from Picbreeder code on GitHub
	 * (though not the original code), but 2 in 13 randomly mutated networks
	 * still produce boring black screens. Is there a way to fix this?
	 * 
	 * @param hsb
	 *            array of HSB color information from CPPN
	 * @return scaled HSB information in float array
	 */
	public static float[] rangeRestrictHSB(double[] hsb) {
		return new float[] { (float) hsb[HUE_INDEX],
				// (float) hsb[SATURATION_INDEX],
				(float) ActivationFunctions.halfLinear(hsb[SATURATION_INDEX]),
				// (float) hsb[BRIGHTNESS_INDEX]};
				(float) Math.abs(hsb[BRIGHTNESS_INDEX]) };
	}

	/**
	 * Gets scaled inputs to send to CPPN
	 *
	 * @param x
	 *            x-coordinate of pixel
	 * @param y
	 *            y-coordinate of pixel
	 * @param imageWidth
	 *            width of image
	 * @param imageHeight
	 *            height of image
	 *
	 * @return array containing inputs for CPPN
	 */
	public static double[] getCPPNInputs(int x, int y, int imageWidth, int imageHeight) {
		ILocated2D scaled = CartesianGeometricUtilities.centerAndScale(new Tuple2D(x, y), imageWidth, imageHeight);
		return new double[] { scaled.getX(), scaled.getY(), scaled.distance(new Tuple2D(0, 0)) * SQRT2, BIAS };
	}

	/**
	 * method for drawing an image onto a drawing panel
	 *
	 * @param image
	 *            image to draw
	 * @param label
	 *            name of image
	 * @param imageWidth
	 *            width of image
	 * @param imageHeight
	 *            height of image
	 *
	 * @return the drawing panel with the image
	 */
	public static DrawingPanel drawImage(BufferedImage image, String label, int imageWidth, int imageHeight) {
		DrawingPanel parentPanel = new DrawingPanel(imageWidth, imageHeight, label);
		Graphics2D parentGraphics = parentPanel.getGraphics();
		parentGraphics.drawRenderedImage(image, null);
		return parentPanel;
	}

	/**
	 * Creates an image of the specified size and height consisting entirely
	 * of a designated solid color.
	 * 
	 * @param c Color throughout image
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @return BufferedImage in solid color
	 */
	public static BufferedImage solidColorImage(Color c, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {// scans across whole image
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, c.getRGB());
			}
		}
		return image;
	}
	
	/**
	 * Plots line of a designated color drawn by an input array list of doubles on a drawing panel.
	 * 
	 * @param panel DrawingPanel
	 * @param min minimum value of score
	 * @param max maximum value of score
	 * @param scores list of doubles to be plotted on graph
	 * @param color Color of line
	 */
	public static void linePlot(DrawingPanel panel, double min, double max, ArrayList<Double> scores, Color color) {
		Graphics g = panel.getGraphics();
		int height = panel.getFrame().getHeight() - 50; // -50 is to avoid gray panel at bottom of DrawingPanel
		int width = panel.getFrame().getWidth();		
		linePlot(g, min, max, height, width, scores, color); // calls secondary linePlot method after necessary info is defined
	}
	
	/**
	 * Creates an image, sets the background to be white, and plots a line on the image.
	 * 
	 * @param height of image
	 * @param width of image
	 * @param min score for scaling
	 * @param max score for scaling
	 * @param scores list of doubles being plotted
	 * @param color of line being plotted
	 * @return BufferedImage formed from plotted line
	 */
	public static BufferedImage linePlotImage(int height, int width, double min, double max, ArrayList<Double> scores, Color color) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		linePlot(g, min, max, height, width, scores, color);
		return bi;
	}
	
	/**
	 * Plots a line of a designated color on a graphics image using an input array list of doubles
	 * 
	 * @param g input graphics to be manipulated
	 * @param min minimum value of score
	 * @param max maximum value of score
	 * @param height height of image being created
	 * @param width width of image being created
	 * @param scores list of doubles to be plotted on graph
	 * @param color Color of line
	 */
	public static void linePlot(Graphics g, double min, double max, int height, int width, ArrayList<Double> scores, Color color) {
		g.setColor(Color.black);
		// y-axis
		g.drawLine(Plot.OFFSET, Plot.OFFSET, Plot.OFFSET, height - Plot.OFFSET);	
		// x-axis
		g.drawLine(Plot.OFFSET, height - Plot.OFFSET, width - Plot.OFFSET, height - Plot.OFFSET);
		double last = scores.get(0);
		double maxRange = Math.max(max, max - min);
		double lowerMin = Math.min(0, min);
		for (int i = 1; i < scores.size(); i++) {
			g.setColor(color);
			// g.fillRect(OFFSET + scale((double) i, (double) scores.size()),
			// OFFSET + invert(scores.get(i), max), 1, 1);
			int x1 = Plot.OFFSET + scale((double) (i - 1), (double) scores.size(), 0, width);
			int y1 = Plot.OFFSET + invert(last, maxRange, lowerMin, height);
			int x2 = Plot.OFFSET + scale((double) i, (double) scores.size(), 0, width);
			int y2 = Plot.OFFSET + invert(scores.get(i), maxRange, lowerMin, height);

			//System.out.println(x1+","+ y1+","+ x2+","+ y2);
			g.drawLine(x1, y1, x2, y2);
			g.setColor(Color.black);
			last = scores.get(i);
		}
		g.drawString("" + max, Plot.OFFSET / 2, Plot.OFFSET / 2);
		g.drawString("" + lowerMin, Plot.OFFSET / 2, height - (Plot.OFFSET / 2));
	}
	
	/**
	 * Creates a graphed visualization of an audio file by taking in the file represented as a list of doubles and 
	 * plotting it using a DrawingPanel.
	 * 
	 * @param fileName String reference to file being plotted
	 */
	public static void wavePlotFromFile(String fileName, int height, int width) {
		double[] fileArray = SoundToArray.read(fileName); //create array of doubles representing audio
		wavePlotFromDoubleArray(fileArray, height, width);
	}
	
	/**
	 *  Creates a graphed visualization of an audio file by taking in the list of doubles that represents the file and 
	 * plotting it using a DrawingPanel.
	 * 
	 * @param inputArray
	 */
	public static BufferedImage wavePlotFromDoubleArray(double[] inputArray, int height, int width) {
		ArrayList<Double> fileArrayList = ArrayUtil.doubleVectorFromArray(inputArray); //convert array into array list
		BufferedImage wavePlot = linePlotImage(height, width, -1.0, 1.0, fileArrayList, Color.black);
		return wavePlot;
	}
	
	/**
	 * Scales x value based on maximum and minimum value. This scale method is based on original browser 
	 * dimension, which is meant for evolution lineage/Ms. Pacman. 
	 * 
	 * @param x Input value
	 * @param max maximum x value
	 * @param min minimum x value
	 * @return scaled x value
	 */
	public static int scale(double x, double max, double min) {
		return scale(x, max, min, Plot.BROWSE_DIM);
	}
	
	/**
	 * Scales x value based on maximum and minimum value. This scale method is more generalized and 
	 * was created specifically for plotting sound waves in StdAudio. 
	 * 
	 * @param x Input value
	 * @param max maximum value
	 * @param min minimum value
	 * @return scaled x value
	 */
	public static int scale(double x, double max, double min, int totalWidth) {
		return (int) (((x - min) / max) * (totalWidth - (2 * Plot.OFFSET)));
	}
	
	/**
	 * Inverts y value based on maximum and minimum value to fit graphical x/y proportions. 
	 * This method is the original invert method intended for evolution lineage/Ms. Pacman. 
	 * 
	 * @param y Input value
	 * @param max maximum value
	 * @param min minimum value
	 * @return scaled x value
	 */
	public static int invert(double y, double max, double min) {
		return invert(y,max,min);
	}
	
	/**
	 * Inverts y value based on maximum and minimum value to fit graphical x/y proportions. 
	 * This method is a secondary invert method created for plotting sound waves in StdAudio
	 *  
	 * @param y Input value
	 * @param max maximum value
	 * @param min minimum value
	 * @return scaled x value
	 */
	public static int invert(double y, double max, double min, int totalHeight) {
		return (totalHeight - (2 * Plot.OFFSET)) - scale(y, max, min, totalHeight);
	}
}
