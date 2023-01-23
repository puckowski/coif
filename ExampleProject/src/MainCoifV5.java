import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

/**
 * Good for images with textures such as leaves or grass
 * 
 * @author Daniel Puckowski
 *
 */
public class MainCoifV5 {
	public static int pixelCount = 0;
	 
	public static List<HistResult> performCircles2(int[][] image, int radius, List<MoravecResult> results,
			final int binMergeCount) {
		List<HistResult> histResult = new ArrayList<HistResult>();

		for (MoravecResult mr : results) {
			CircleResult cr = circles3(image, mr.getX(), mr.getY(), radius);
			int[] hist = cr.getHist();
			int[] hist2 = cr.getInnerHist();
			int[] center = cr.getCenterHist();

			HistResult hr = new HistResult(hist, hist2, mr.getX(), mr.getY(), center);
			hr.computeDistances(binMergeCount);
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
		
		final int radiusSquaredHalf2 = radiusSquared / 7;

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
	
	public static void process(final String file1, final String file2, final int fileIndex)
			throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Moravec step...");

		int thresholdmor = 100;
		int ptsmor = 40;

		MoravecProcessor moravecProcessor = new MoravecProcessor(thresholdmor, ptsmor, 0.02);
		List<MoravecResult> morResults;
		MoravecProcessor moravecProcessor2 = new MoravecProcessor(thresholdmor, ptsmor, 0.02);
		List<MoravecResult> morResults2;
		try {
			moravecProcessor.process(file1);
			moravecProcessor2.process(file2);

			if (!file1.contains("_rot") && !file2.contains("_rot")) {
				moravecProcessor.average(moravecProcessor2.getGrayscaleData());
			}

			moravecProcessor.process2();
			moravecProcessor2.process2();

			morResults = moravecProcessor.getResults();
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

		pixelCount += width * height;
		pixelCount += width2 * height2;

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

		long startTime = System.currentTimeMillis();

		List<FeatureMatch> featureMatches = new ArrayList<FeatureMatch>();
		int binMergeCount = 1;

		do {
			System.out.println("Circles step...");

			if (binMergeCount > 5) {
				break;
			}

			featureMatches.clear();

			int circleSize = 30;

			System.out.println("Bin merge count: " + binMergeCount);

			List<HistResult> hrlist = performCircles2(image, circleSize, morResults, binMergeCount);
			List<HistResult> hrlist2 = performCircles2(image2, circleSize, morResults2, binMergeCount);

			binMergeCount++;

			System.out.println("Circles step done.");
			System.out.println("Feature matching step...");

			final int binThreshold =9; // or 11 or 7
			final double binLowerBoundPercent = 0.98;
			final double binUpperBoundPercent = 1.02;
			int binThreshold2 = 15; // or 17 or 13

			int matchingIndex = 0;
			int[] distancesFirst;
			int[] distancesSecond;
			int[] dist21;
			int[] dist22;
			int binDistance;

			final int maximumDifferenceThreshold = 40;
			
			for (int i = 0; i < hrlist.size(); ++i) {
				if (hrlist.get(i).mDistinctiveness < 80) {
					hrlist.remove(i);
					--i;
				}
			}

			for (int i = 0; i < hrlist2.size(); ++i) {
				if (hrlist2.get(i).mDistinctiveness < 80) {
					hrlist2.remove(i);
					--i;
				}
			}

			if (hrlist.size() > 10000) {
				for (int i = 0; i < hrlist.size(); ++i) {
					if (hrlist.get(i).mDistinctiveness < 105) {
						hrlist.remove(i);
						--i;
					}
				}
			}

			if (hrlist2.size() > 10000) {
				for (int i = 0; i < hrlist2.size(); ++i) {
					if (hrlist2.get(i).mDistinctiveness < 105) {
						hrlist2.remove(i);
						--i;
					}
				}
			}

			while (hrlist.size() > 20000) {
				int randomIndex = ThreadLocalRandom.current().nextInt(0, hrlist.size());
				hrlist.remove(randomIndex);
			}

			while (hrlist2.size() > 20000) {
				int randomIndex = ThreadLocalRandom.current().nextInt(0, hrlist2.size());
				hrlist2.remove(randomIndex);
			}

			System.out.println("Histogram result counts: " + hrlist.size() + ", " + hrlist2.size());

			double val, val2, valLow, valThresholdCheck, valHigh, valThresholdCheckHigh;
			int i, roughdist;

			for (HistResult hr : hrlist) {
				distancesFirst = hr.getDistances();
				dist21 = hr.getDistances2();
				
				for (HistResult hr2 : hrlist2) {
					if (hr2.mDistinctiveness < hr.mMinDistinctiveness
							|| hr2.mDistinctiveness > hr.mMaxDistinctiveness) {
						continue;
					}
					
					roughdist = 0;

					distancesSecond = hr2.getDistances();
					dist22 = hr2.getDistances2();
					binDistance = 0;

					for (i = 0; i < distancesFirst.length; ++i) {
						val = distancesFirst[i];
						val2 = distancesSecond[i];

						valLow = (val * binLowerBoundPercent);
						valThresholdCheck = Math.abs(val - valLow);
						if (valThresholdCheck > maximumDifferenceThreshold)
							valLow = val - maximumDifferenceThreshold;
						valHigh = (val * binUpperBoundPercent);
						valThresholdCheckHigh = Math.abs(val - valHigh);
						if (valThresholdCheckHigh > maximumDifferenceThreshold)
							valHigh = val + maximumDifferenceThreshold;

						if (val2 < valLow || val2 > valHigh) {
							binDistance++;
							roughdist++;

							if (Math.abs(val2 - val) < binThreshold2) {
								binDistance--;
							}

							if (binDistance >= binThreshold) {
								break;
							}
						}
					}

					for (i = 0; i < dist21.length && binDistance < binThreshold; ++i) {
						val = dist21[i];
						val2 = dist22[i];

						valLow = (val * binLowerBoundPercent);
						valThresholdCheck = Math.abs(val - valLow);
						if (valThresholdCheck > maximumDifferenceThreshold)
							valLow = val - maximumDifferenceThreshold;
						valHigh = (val * binUpperBoundPercent);
						valThresholdCheckHigh = Math.abs(val - valHigh);
						if (valThresholdCheckHigh > maximumDifferenceThreshold)
							valHigh = val + maximumDifferenceThreshold;

						if (val2 < valLow || val2 > valHigh) {
							binDistance++;
							roughdist++;

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
						f.roughBinDistance = roughdist;// binDistance;
						
						featureMatches.add(f);

						g2d.setColor(Color.RED);
						g2d.drawString(String.valueOf(hr.mDistinctiveness), hr.getX() + 10, hr.getY() + 10);//hr.mDistinctiveness), hr.getX() + 10, hr.getY() + 10);
						g2d.drawString(String.valueOf(hr2.mDistinctiveness), hr2.getX() + 10 + width, hr2.getY() + 10);//hr2.mDistinctiveness), hr2.getX() + 10 + width, hr2.getY() + 10);

						// if (featureMatches.size() > 50) {
						// break;
						// }
					}
				}

				matchingIndex++;

				if (matchingIndex % 1000 == 0) {
					System.out.println("Histograms compared: " + matchingIndex + " / " + hrlist.size());
				}

				// if (featureMatches.size() > 50) {
				// break;
				// }
			}

			for (int in = 0; in < featureMatches.size(); ++in) {
				if (featureMatches.get(in).roughBinDistance > 30) {
					featureMatches.remove(in);
					in--;
				}
			}

			System.out.println("Feature matching done.");
		} while (featureMatches.size() < 5
				|| evaluateFeatureMatchCloseness(featureMatches) >= (featureMatches.size() * 0.8));

		TimeData.binDistanceUsage[binMergeCount - 1]++;
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		TimeData.matching += estimatedTime;
		TimeData.matchingTimes.add(estimatedTime);

		System.out.println("Finalizing montage...");

		PrintWriter writer = new PrintWriter("moravec3_" + String.valueOf(fileIndex) + ".txt", "UTF-8");
		writer.println(file1);
		writer.println(file2);

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

			writer.println(fm.getX1());
			writer.println(fm.getY1());
			writer.println(fm.getX2());
			writer.println(fm.getY2());
		}

		writer.close();

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

	public static int evaluateFeatureMatchCloseness(final List<FeatureMatch> featureMatches) {
		System.out.println("Evaluating feature match closeness...");

		if (featureMatches.size() > 50) {
			System.out.println("Feature match closeness: 0.");
			return 0;
		}

		int allMatches = 0;

		for (int i = 0; i < featureMatches.size(); ++i) {
			final FeatureMatch fm = featureMatches.get(i);
			int matches = 0;

			for (int n = 0; n < featureMatches.size(); ++n) {
				if (i == n) {
					continue;
				}

				final FeatureMatch fm2 = featureMatches.get(n);

				int x = fm.getX1();
				int y = fm.getY1();

				if (fm2.getX2() >= ((double) x * 0.9) && fm2.getX2() <= ((double) x * 1.1)) {
					if (fm2.getY2() >= ((double) y * 0.9) && fm2.getY2() <= ((double) y * 1.1)) {
						matches++;
					}
				}
			}

			if (matches >= 4) {
				allMatches++;
			}
		}

		System.out.println("Feature match closeness: " + allMatches + ".");
		return allMatches;
	}

	public static void main(String[] args) throws IOException {
		final String[] files1 = { "Test404.jpg", "Test705.jpg", "Test705.jpg", "Test766.jpg", "Test766.jpg", "Test82.jpg",
				"Test5000_rot.jpg", "Test3000_rot.jpg", "Test3000_rot.jpg", "Test1500.jpg", "Test1310_rot.PNG",
				"Test1199_rot.PNG", "Test1000_rot.jpg", "Test2120_rot.jpg", "Test1999_rot.jpg", "Test4.jpg",
				"Test6.jpg", "Test21.jpg", "Test34_rot.jpg", "Test37.jpg", "Test47_rot.jpg", "Test48_rot.png",
				"Test65.jpg", "Test70.jpg", "Test99.jpg", "Test120_rot.jpeg", "Test121_rot.png", "Test122_rot.png",
				"Test123_rot.jpg", "Test200_rot.jpg", "Test211_rot.jpg", "Test240.jpg", "Test300.jpg", "Test400.jpg",
				"Test600.jpg", "Test800.jpg" };
		final String[] files2 = { "Test405.jpg", "Test706.jpg", "Test707.jpg", "Test767.jpg", "Test768.jpg", "Test81.jpg",
				"Test5001_rot.jpg", "Test3002_rot.jpg", "Test3001_rot.jpg", "Test1501.jpg", "Test1311_rot.PNG",
				"Test1200_rot.PNG", "Test1001_rot.jpg", "Test2121_rot.jpg", "Test2000_rot.jpg", "Test5.jpg",
				"Test7.jpg", "Test22.jpg", "Test35_rot.jpg", "Test38.jpg", "Test48_rot.jpg", "Test49_rot.png",
				"Test66.jpg", "Test71.jpg", "Test100.jpg", "Test124_rot.jpeg", "Test125_rot.png", "Test126_rot.png",
				"Test127_rot.jpg", "Test201_rot.jpg", "Test212_rot.jpg", "Test241.jpg", "Test310.jpg", "Test410.jpg",
				"Test610.jpg", "Test810.jpg" };

		for (int i = 0; i < files1.length; ++i) {
			System.out.println("Processing " + files1[i] + " and " + files2[i]);
			process(files1[i], files2[i], i);
		}

		System.out.println((double) TimeData.imageLoad / 1000.0 + "s loading images");
		System.out.println((double) TimeData.moravec / 1000.0 + "s finding corners");
		System.out.println((double) TimeData.matching / 1000.0 + "s matching features");
		System.out.println((TimeData.imageLoad) / files1.length + "ms loading image average");
		System.out.println((TimeData.moravec) / files1.length + "ms finding corner average");
		System.out.println((TimeData.matching) / files1.length + "ms matching feature average");
		System.out.println(pixelCount + " pixels processed");

		long medianMoravec = TimeData.moravecTimes.stream().map(Long::valueOf).sorted()
				.collect(Collectors.collectingAndThen(Collectors.toList(), times -> {
					int count = times.size();
					if (count % 2 == 0) {
						return (times.get(count / 2 - 1) + times.get(count / 2)) / 2;
					} else {
						return times.get(count / 2);
					}
				}));
		long medianMatching = TimeData.matchingTimes.stream().map(Long::valueOf).sorted()
				.collect(Collectors.collectingAndThen(Collectors.toList(), times -> {
					int count = times.size();
					if (count % 2 == 0) {
						return (times.get(count / 2 - 1) + times.get(count / 2)) / 2;
					} else {
						return times.get(count / 2);
					}
				}));
		System.out.println(((double) medianMoravec) + "ms finding corner median");
		System.out.println(((double) medianMatching) + "ms matching feature median");

		List<Double> moravecTimes = TimeData.moravecTimes.stream().map(Double::valueOf).collect(Collectors.toList());
		List<Double> matchingTimes = TimeData.matchingTimes.stream().map(Double::valueOf).collect(Collectors.toList());
		final Set<Double> moravecOutliers = eliminateOutliers(moravecTimes, 2f).stream().collect(Collectors.toSet());
		final Set<Double> matchingOutliers = eliminateOutliers(matchingTimes, 2f).stream().collect(Collectors.toSet());
		System.out.println("Corner outliers: " + moravecOutliers.toString());
		System.out.println("Feature matching outliers: " + matchingOutliers.toString());
		moravecTimes = moravecTimes.stream().filter(time -> !moravecOutliers.contains(time))
				.collect(Collectors.toList());
		matchingTimes = matchingTimes.stream().filter(time -> !matchingOutliers.contains(time))
				.collect(Collectors.toList());
		final Double moravecSum = moravecTimes.stream().mapToDouble(f -> f.doubleValue()).sum();
		final Double matchingSum = matchingTimes.stream().mapToDouble(f -> f.doubleValue()).sum();
		System.out.println(moravecSum / ((double) files1.length) + "ms finding corner average without outliers");
		System.out.println(matchingSum / ((double) files1.length) + "ms matching feature average without outliers");

		System.out.println(((double) TimeData.moravec + (double) TimeData.matching) / 1000.0 + "s total");
		System.out.println(((TimeData.matching) + TimeData.moravec) / files1.length + "ms average");
		System.out.println(((double) medianMatching + (double) medianMoravec) + "ms median");
		System.out.println((moravecSum + matchingSum) / ((double) files1.length) + "ms average without outliers");
		
		System.out.println("Merge distances used:");
		for (int i = 1; i < TimeData.binDistanceUsage.length; ++i) {
			System.out.println(i + " used " + TimeData.binDistanceUsage[i] + " times");
		}
	}

	protected static double getMean(List<Double> values) {
		int sum = 0;
		for (double value : values) {
			sum += value;
		}

		return (sum / values.size());
	}

	public static double getVariance(List<Double> values) {
		double mean = getMean(values);
		int temp = 0;

		for (double a : values) {
			temp += (a - mean) * (a - mean);
		}

		return temp / (values.size() - 1);
	}

	public static double getStdDev(List<Double> values) {
		return Math.sqrt(getVariance(values));
	}

	public static List<Double> eliminateOutliers(List<Double> values, float scaleOfElimination) {
		double mean = getMean(values);
		double stdDev = getStdDev(values);

		final List<Double> newList = new ArrayList<>();

		for (double value : values) {
			boolean isLessThanLowerBound = value < mean - stdDev * scaleOfElimination;
			boolean isGreaterThanUpperBound = value > mean + stdDev * scaleOfElimination;
			boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

			if (!isOutOfBounds) {
				newList.add(value);
			}
		}

		int countOfOutliers = values.size() - newList.size();
		if (countOfOutliers == 0) {
			return values;
		}

		return eliminateOutliers(newList, scaleOfElimination);
	}

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
