package similarity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ModifiedAnalyzer {
	
	// The size cap of the images.
	private static final int sizeCap = 300;
	// The final size of the BufferedImages to compare (width = height = scaledSize).
	public int scaledSize;
	
	/*
	 * Returns the difference between images as a double.
	 * The closer to 0 the value is, the more similar the images are.
	 * A value of 0 means the images are equal.
	 */
	public double compare(BufferedImage bx, BufferedImage by) {
		scaledSize = getAppropriateScale(bx,by);

		BufferedImage xscaled = rescale(bx, scaledSize, scaledSize, true);
		BufferedImage yscaled = rescale(by, scaledSize, scaledSize, true);

		return calcDistance(xscaled, yscaled);
	}
	
	/*
	 * Returns the difference between images as a double.
	 * The closer to 0 the value is, the more similar the images are.
	 * A value of 0 means the images are equal.
	 */
	public double compare(File fx, File fy) throws IOException {
		BufferedImage bx = ImageIO.read(fx);
		BufferedImage by = ImageIO.read(fy);
		return compare(bx,by);
	}	
	
	/*
	 * Returns true if the difference between images is less than the threshold.
	 */
	public boolean compareThreshold(BufferedImage bx, BufferedImage by, double threshold) {
		return compare(bx,by) <= threshold;
	}
	
	/*
	 * Returns true if the difference between images is less than the threshold.
	 */
	public boolean compareThreshold(File fx, File fy, double threshold) throws IOException {
		BufferedImage bx = ImageIO.read(fx);
		BufferedImage by = ImageIO.read(fy);
		return compare(bx,by) <= threshold;
	}

	/*
	 * The reference image "signature" (25 representative pixels, each in R,G,B).
	 * We use instances of Color to make things simpler.
	 */
	public Color[][] calcSignature(BufferedImage i) {
		// Get memory for the signature.
		Color[][] sig = new Color[5][5];
		// For each of the 25 signature values average the pixels around it.
		// Note that the coordinate of the central pixel is in proportions.
		float[] prop = new float[] { 1f / 10f, 3f / 10f, 5f / 10f, 7f / 10f, 9f / 10f };
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				sig[x][y] = averageAround(i, prop[x], prop[y]);
			}
		return sig;
	}
	
	public double quickDistance(Color[][] sigx, Color[][] sigy) {
		double dist = 0;
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				int r1 = sigx[x][y].getRed();
				int g1 = sigx[x][y].getGreen();
				int b1 = sigx[x][y].getBlue();
				int r2 = sigy[x][y].getRed();
				int g2 = sigy[x][y].getGreen();
				int b2 = sigy[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
	}

	public double calcDistance(BufferedImage bx, BufferedImage by) {
		// Calculate the signature for the images.
		Color[][] sigx = calcSignature(bx);
		Color[][] sigy = calcSignature(by);
		// There are several ways to calculate distances between two vectors,
		// we will calculate the sum of the distances between the RGB values of
		// pixels in the same positions.
		double dist = 0;
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				int r1 = sigx[x][y].getRed();
				int g1 = sigx[x][y].getGreen();
				int b1 = sigx[x][y].getBlue();
				int r2 = sigy[x][y].getRed();
				int g2 = sigy[x][y].getGreen();
				int b2 = sigy[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
	}

	private Color averageAround(BufferedImage i, double px, double py) {
		// Get memory for a pixel and for the accumulator.
		double[] pixel;
		double[] accum = new double[3];
		// The size of the sampling area.
		double sampleSize = 0.09 * scaledSize;
		int numPixels = 0;
		// Sample the pixels.
		for (double x = px * scaledSize - sampleSize; x < px * scaledSize + sampleSize; x++) {
			for (double y = py * scaledSize - sampleSize; y < py * scaledSize + sampleSize; y++) {
				Color pix = new Color(i.getRGB(new Double(x).intValue(), new Double(y).intValue()));
				pixel = new double[] { pix.getBlue(), pix.getGreen(), pix.getRed() };
				accum[0] += pixel[0];
				accum[1] += pixel[1];
				accum[2] += pixel[2];
				numPixels++;
			}
		}
		// Average the accumulated values.
		accum[0] /= numPixels;
		accum[1] /= numPixels;
		accum[2] /= numPixels;
		return new Color((int) accum[0], (int) accum[1], (int) accum[2]);
	}
	
	
	/*
	 * This method finds the lowest possible scaledSize value.
	 */
	private int getAppropriateScale(BufferedImage bx, BufferedImage by) {
		int r = bx.getWidth();
		if (r>sizeCap) r = sizeCap;
		if (r>bx.getHeight()) r = bx.getHeight();
		if (r>by.getWidth()) r = by.getWidth();
		if (r>by.getHeight()) r = by.getHeight();
		return r;
	}

	/*
	 * This method resizes an image through a sequence of rescalings.
	 */
	public BufferedImage rescale(BufferedImage img, int targetWidth, int targetHeight,
			boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w < targetWidth) {
				w *= 1.2;
			}

			if (higherQuality && h < targetHeight) {
				h *= 1.2;
			}

			if (higherQuality && w > targetWidth) {
				w /= 2.5;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2.5;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}
}
