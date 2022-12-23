import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

/**
 * Good for images with textures such as leaves or grass where
 * MainCoifV2ForTextures produces too few matches
 * 
 * @author Daniel Puckowski
 *
 */
public class MainCoifV2ForTexturesWithTwoHists {

	public static List<HistResult> performCircles2(int[][] image, int radius, List<MoravecResult> results) {
		List<HistResult> histResult = new ArrayList<HistResult>();

		for (MoravecResult mr : results) {
			CircleResult cr = circles3(image, mr.getX(), mr.getY(), radius);
			int[] hist = cr.getHist();
			int[] hist2 = cr.getInnerHist();
			int[] hist3 = cr.getInnermostHist();

			HistResult hr = new HistResult(hist, hist2, mr.getX(), mr.getY(), hist3);
			int ssd = sumSquareDiff2(hr);
			hr.setSsd(ssd);
			hr.computeDistances();
			hr.computeDistinctiveness(2);

			histResult.add(hr);
		}

		return histResult;
	}

	public static CircleResult circles3(int[][] image, final int circleX, final int circleY, final int radius) {
		final int width = image.length;
		final int height = image[0].length;
		final int radiusSquared = radius * radius;
		final int radiusSquaredHalf = radiusSquared / 3;

		final int radiusSquaredHalf2 = radiusSquared / 7; // 4

		int[] hist = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist[i] = 0;
		}

		int[] hist2 = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist2[i] = 0;
		}

		int[] hist3 = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist3[i] = 0;
		}

		for (int x = circleX - radius - 3; x < circleX + radius + 3; x++) {
			if (x < 0 || x >= width)
				continue;

			for (int y = circleY - radius - 3; y < circleY + radius + 3; y++) {
				if (y < 0 || y >= height)
					continue;

				double dx = x - circleX;
				double dy = y - circleY;
				double distanceSquared = dx * dx + dy * dy;

				if (distanceSquared <= radiusSquared) {
					int val = image[x][y];
					hist[val]++;

					if (distanceSquared <= (radiusSquaredHalf)) {
						hist2[val]++;

						if (distanceSquared <= (radiusSquaredHalf2)) {
							hist3[val]++;
						}
					}
				}
			}
		}

		CircleResult circleResult = new CircleResult(hist, hist2, hist3);

		return circleResult;
	}

	public static int sumSquareDiff2(HistResult res1) {
		int[] hist1 = res1.getHist();
		int[] hist2 = res1.getInnerHist();

		int ssd = 0;
		for (int i = 0; i < hist1.length; ++i) {
			int diff = hist1[i] - hist2[i];
			ssd += diff * diff;
		}

		return ssd;
	}

	public static double calculateStandardDeviation(List<HistResult> hrlist) {
		double sum = 0.0, standardDeviation = 0.0;
		int length = hrlist.size();

		for (int x = 0; x < hrlist.size(); ++x) {
			sum += hrlist.get(x).mDistinctiveness;
		}

		double mean = sum / length;

		for (int x = 0; x < hrlist.size(); ++x) {
			standardDeviation += Math.pow(hrlist.get(x).mDistinctiveness - mean, 2);
		}

		return Math.sqrt(standardDeviation / length);
	}

	public static void process(final String file1, final String file2, final int fileIndex) {
		System.out.println("Moravec step...");

		int thresholdmor = 4000;
		if (file1.contains("rot")) {
			thresholdmor = 2000;
		}
		int ptsmor = 40;
		if (file1.contains("rot")) {
			ptsmor = 80;
		}

		MoravecProcessor moravecProcessor = new MoravecProcessor(8, thresholdmor, ptsmor, 0.02);
		List<MoravecResult> morResults;
		MoravecProcessor moravecProcessor2 = new MoravecProcessor(8, thresholdmor, ptsmor, 0.02);
		List<MoravecResult> morResults2;
		try {
			moravecProcessor.process(file1);
			morResults = moravecProcessor.getResults();

			moravecProcessor2.process(file2);
			morResults2 = moravecProcessor2.getResults();
		} catch (IOException ioException) {
			ioException.printStackTrace();

			System.err.println("Failed to process image.");

			return;
		}

		int[][] image = moravecProcessor.getGrayscaleData();
		int[][] image2 = moravecProcessor2.getGrayscaleData();

		System.out.println("Moravec step done.");

		final int width = image.length;
		final int height = image[0].length;
		final int width2 = image2.length;
		final int height2 = image2[0].length;

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int val = (int) Math.ceil((double) image[x][y] * 0.5);
				image[x][y] = val;
			}
		}

		for (int x = 0; x < width2; ++x) {
			for (int y = 0; y < height2; ++y) {
				int val = (int) Math.ceil((double) image2[x][y] * 0.5);
				image2[x][y] = val;
			}
		}

		System.out.println("Circles step...");

		List<HistResult> hrlist = performCircles2(image, 18, morResults);
		List<HistResult> hrlist2 = performCircles2(image2, 18, morResults2);

		System.out.println("Circles step done.");
		System.out.println("Misc drawing step...");

		BufferedImage newImage3 = new BufferedImage(width + width2, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width + width2; ++x) {
			for (int y = 0; y < height; ++y) {
				if (x < width) {
					Color newColor = new Color(image[x][y], image[x][y], image[x][y]);
					newImage3.setRGB(x, y, newColor.getRGB());
				} else {
					Color newColor = new Color(image2[x - width][y], image2[x - width][y], image2[x - width][y]);
					newImage3.setRGB(x, y, newColor.getRGB());
				}
			}
		}

		Graphics2D g2d = newImage3.createGraphics();

		System.out.println("Misc drawing done.");
		System.out.println("Feature matching step...");

		List<FeatureMatch> featureMatches = new ArrayList<FeatureMatch>();

		final int binThreshold = 6;
		final double binLowerBoundPercent = 0.98;
		final double binUpperBoundPercent = 1.02;
		int binThreshold2 = 3;

		int matchingIndex = 0;
		int[] distancesFirst;
		int[] distancesSecond;
		int binDistance;

		int maximumDifferenceThreshold = 6;

		int stddev1 = (int) Math.round(calculateStandardDeviation(hrlist));
		int stddev2 = (int) Math.round(calculateStandardDeviation(hrlist2));

		int dhist[] = new int[256];
		for (int i = 0; i < hrlist.size(); ++i) {
			dhist[hrlist.get(i).mDistinctiveness]++;
		}
		int dhist2[] = new int[256];
		for (int i = 0; i < hrlist2.size(); ++i) {
			dhist2[hrlist2.get(i).mDistinctiveness]++;
		}

		int highest1 = 0;
		int highval = 0;
		for (int i = 0; i < 256; ++i) {
			if (dhist[i] > highval) {
				highval = dhist[i];
				highest1 = i;
			}
		}

		int highest2 = 0;
		highval = 0;
		for (int i = 0; i < 256; ++i) {
			if (dhist2[i] > highval) {
				highval = dhist2[i];
				highest2 = i;
			}
		}

		int dist1low = (int) highest1 - stddev1;
		int dist1high = (int) highest1 + stddev1;

		int dist2low = (int) highest2 - stddev2;
		int dist2high = (int) highest2 + stddev2;

		for (int i = 0; i < hrlist.size(); ++i) {
			System.out.println(dist1low + " " + dist1high + " " + highest1 + " " + stddev1);

			if (hrlist.get(i).mDistinctiveness <= dist1high && hrlist.get(i).mDistinctiveness >= dist1low) { // 75
				hrlist.remove(i);
				--i;
			} else if (hrlist.get(i).mDistinctiveness < 40) {
				hrlist.remove(i);
				--i;
			}
		}
		for (int i = 0; i < hrlist2.size(); ++i) {
			System.out.println(dist2low + " " + dist2high + " " + highest2 + " " + stddev2);

			if (hrlist2.get(i).mDistinctiveness <= dist2high && hrlist2.get(i).mDistinctiveness >= dist2low) { // 75
				hrlist2.remove(i);
				--i;
			} else if (hrlist2.get(i).mDistinctiveness < 40) {
				hrlist2.remove(i);
				--i;
			}
		}

		for (HistResult hr : hrlist) {
			distancesFirst = hr.getDistances();

			for (HistResult hr2 : hrlist2) {
				distancesSecond = hr2.getDistances();
				binDistance = 0;

				for (int i = 0; i < distancesFirst.length; ++i) {
					double val = distancesFirst[i];
					double val2 = distancesSecond[i];

					double valLow = (val * binLowerBoundPercent);
					double valThresholdCheck = Math.abs(val - valLow);
					if (valThresholdCheck > maximumDifferenceThreshold)
						valLow = val - maximumDifferenceThreshold;
					double valHigh = (val * binUpperBoundPercent);
					double valThresholdCheckHigh = Math.abs(val - valHigh);
					if (valThresholdCheckHigh > maximumDifferenceThreshold)
						valHigh = val + maximumDifferenceThreshold;

					if (val2 < valLow || val2 > valHigh) {
						binDistance++;

						if (Math.abs(val2 - val) < binThreshold2) {
							binDistance--;
						}

						if (binDistance >= binThreshold) {
							break;
						}
					}
				}

				if (binDistance < binThreshold) {
					FeatureMatch f = new FeatureMatch(hr.getX(), hr.getY(), hr2.getX(), hr2.getY());

					featureMatches.add(f);

					g2d.setColor(Color.RED);
					g2d.drawString(String.valueOf(hr.mDistinctiveness), hr.getX() + 10, hr.getY() + 10);
					g2d.drawString(String.valueOf(hr2.mDistinctiveness), hr2.getX() + 10 + width, hr2.getY() + 10);
				}
			}

			matchingIndex++;
			System.out.println(matchingIndex + " / " + hrlist.size());
		}

		System.out.println("Feature matching done.");
		System.out.println("Finalizing montage...");

		for (FeatureMatch fm : featureMatches) {
			int redrand = ThreadLocalRandom.current().nextInt(0, 255 + 1);
			int greenrand = ThreadLocalRandom.current().nextInt(0, 255 + 1);
			int bluerand = ThreadLocalRandom.current().nextInt(0, 255 + 1);
			g2d.setColor(new Color(redrand, greenrand, bluerand));

			drawArrowLine(g2d, fm.getX1(), fm.getY1(), fm.getX2() + width, fm.getY2(), 12, 12);

			g2d.setColor(Color.RED);
			g2d.fillOval(fm.getX1() - 3, fm.getY1() - 3, 6, 6);

			g2d.setColor(Color.GREEN);
			g2d.fillOval(fm.getX2() + width - 3, fm.getY2() - 3, 6, 6);
		}

		System.out.println("Montage finalized.");

		if (newImage3 != null) {
			try {
				File outputfile = new File("moravec3_" + String.valueOf(fileIndex) + ".png");
				ImageIO.write(newImage3, "png", outputfile);
			} catch (IOException ioException) {
				ioException.printStackTrace();

				System.err.println("Failed to write moravec.png");

				return;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		final String[] files1 = { "Test22.jpg" };
		final String[] files2 = { "Test21.jpg" };

		for (int i = 0; i < files1.length; ++i) {
			process(files1[i], files2[i], i);
		}
	}

	/**
	 * Draw an arrow line between two points.
	 * 
	 * @param g  the graphics component.
	 * @param x1 x-position of first point.
	 * @param y1 y-position of first point.
	 * @param x2 x-position of second point.
	 * @param y2 y-position of second point.
	 * @param d  the width of the arrow.
	 * @param h  the height of the arrow.
	 */
	private static void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
		int dx = x2 - x1, dy = y2 - y1;
		double D = Math.sqrt(dx * dx + dy * dy);
		double xm = D - d, xn = xm, ym = h, yn = -h, x;
		double sin = dy / D, cos = dx / D;

		x = xm * cos - ym * sin + x1;
		ym = xm * sin + ym * cos + y1;
		xm = x;

		x = xn * cos - yn * sin + x1;
		yn = xn * sin + yn * cos + y1;
		xn = x;

		int[] xpoints = { x2, (int) xm, (int) xn };
		int[] ypoints = { y2, (int) ym, (int) yn };

		g.drawLine(x1, y1, x2, y2);
		g.fillPolygon(xpoints, ypoints, 3);
	}

}
