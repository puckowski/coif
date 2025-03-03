package coifv7;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class MainCoifV6BestMatches {
	public static int pixelCount = 0;

	public static List<HistResultList> performCircles2(int[][] image, int radius, List<MoravecResult> results,
			final int binMergeCount) {
		final List<HistResultList> resultList = new ArrayList<>();
		final int width = image.length;
		final int height = image[0].length;

		for (MoravecResult mr : results) {
			int x = mr.getX();
			int y = mr.getY();

			if (x - 4 >= 0 && y - 4 >= 0 && x + 4 < width && y + 4 < height) {
				CircleResult cr = circles3(image, mr.getX(), mr.getY() - 4, radius);
				int[] hist = cr.getHist();
				int[] hist2 = cr.getInnerHist();
				int[] center = cr.getCenterHist();

				HistResult hr = new HistResult(hist, hist2, mr.getX(), mr.getY() - 4, center);
				hr.computeDistances(binMergeCount);
				hr.computeDistinctiveness(2);

				CircleResult crSec = circles3(image, mr.getX() - 4, mr.getY(), radius);
				int[] histSec = crSec.getHist();
				int[] hist2Sec = crSec.getInnerHist();
				int[] centerSec = crSec.getCenterHist();

				HistResult hrSec = new HistResult(histSec, hist2Sec, mr.getX() - 4, mr.getY(), centerSec);
				hrSec.computeDistances(binMergeCount);
				hrSec.computeDistinctiveness(2);

				CircleResult crThird = circles3(image, mr.getX() + 4, mr.getY(), radius);
				int[] histThird = crThird.getHist();
				int[] hist2Third = crThird.getInnerHist();
				int[] centerThird = crThird.getCenterHist();

				HistResult hrThird = new HistResult(histThird, hist2Third, mr.getX() + 4, mr.getY(), centerThird);
				hrThird.computeDistances(binMergeCount);
				hrThird.computeDistinctiveness(2);

				CircleResult crFourth = circles3(image, mr.getX(), mr.getY() + 4, radius);
				int[] histFourth = crFourth.getHist();
				int[] hist2Fourth = crFourth.getInnerHist();
				int[] centerFourth = crFourth.getCenterHist();

				HistResult hrFth = new HistResult(histFourth, hist2Fourth, mr.getX(), mr.getY() + 4, centerFourth);
				hrFth.computeDistances(binMergeCount);
				hrFth.computeDistinctiveness(2);

				HistResultList histResultList = new HistResultList();
				histResultList.histResults.add(hr);
				histResultList.histResults.add(hrSec);
				histResultList.histResults.add(hrThird);
				histResultList.histResults.add(hrFth);

				resultList.add(histResultList);
			}
		}

		return resultList;
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
		Color newColor;
		
		for (int x = 0; x < width + width2; ++x) {
			for (int y = 0; y < height; ++y) {
				if (x < width) {
					newColor = new Color(image[x][y], image[x][y], image[x][y]);
					newImage3.setRGB(x, y, newColor.getRGB());
				} else {
					newColor = new Color(image2[x - width][y], image2[x - width][y], image2[x - width][y]);
					newImage3.setRGB(x, y, newColor.getRGB());
				}
			}
		}

		Graphics2D g2d = newImage3.createGraphics();

		System.out.println("Misc drawing done.");

		long startTime = System.currentTimeMillis();

		List<FeatureMatch> featureMatches = new ArrayList<FeatureMatch>();

		double binThreshold2Negation;

		int matchingIndex;
		int[] distancesFirst;
		int[] distancesSecond;
		int[] dist21;
		int[] dist22;
		int binDistance;

		int circleSize = 30;

		double mod;
		long count;
		double sum, high, quart;

		double val, val2;
		int i;

		int[][] compareIndexArray = { { 0, 1, 2, 3 }, { 1, 2, 3, 0 }, { 2, 3, 0, 1 }, { 3, 0, 1, 2 }, };
		int lowestDistance, compareIndex, compareIndexMatch, distanceFinal, hri;
		HistResult result1, result2;
		final Map<Integer, Integer> rotationIndexMap = new HashMap<Integer, Integer>();
		int maxKey, maxValue;

		HistResultList list1;

		int binThreshold = 38;
		int binMergeCount = 1;
		int binThreshold2 = 57;

		do {
			binMergeCount = 1;
			binThreshold += 2;
			binThreshold2 += 3;
			binThreshold2Negation = binThreshold2 * 0.85;

			do {
				System.out.println("Circles step...");

				if (binMergeCount > 5) {
					break;
				}

				featureMatches.clear();

				matchingIndex = 0;

				System.out.println("Bin merge count: " + binMergeCount);

				List<HistResultList> hrlist = performCircles2(image, circleSize, morResults, binMergeCount);
				List<HistResultList> hrlist2 = performCircles2(image2, circleSize, morResults2, binMergeCount);

				binMergeCount++;

				System.out.println("Circles step done.");
				System.out.println("Feature matching step...");

				mod = 0.35;

				do {
					mod -= 0.05;
					sum = 0.0;
					count = 0;

					for (i = 0; i < hrlist.size(); ++i) {
						HistResultList hr = hrlist.get(i);
						for (HistResult h : hr.histResults) {
							sum += h.mDistinctiveness;
							count++;
						}
					}

					sum /= count;
					quart = sum * mod;
					high = sum + quart;
					count = 0;

					for (i = 0; i < hrlist.size(); ++i) {
						HistResultList hr = hrlist.get(i);
						sum = 0.0;
						for (HistResult h : hr.histResults) {
							sum += h.mDistinctiveness;
						}
						sum /= hr.histResults.size();

						if (sum < high) {
							count++;
						}
					}
				} while (hrlist.size() - count < 2500 && hrlist.size() > 2500);

				for (i = 0; i < hrlist.size(); ++i) {
					HistResultList hr = hrlist.get(i);
					sum = 0.0;
					for (HistResult h : hr.histResults) {
						sum += h.mDistinctiveness;
					}
					sum /= hr.histResults.size();

					if (sum < high) {
						hrlist.remove(i);
						--i;
					}
				}

				mod = 0.35;

				do {
					mod -= 0.05;
					sum = 0.0;
					count = 0;

					for (i = 0; i < hrlist2.size(); ++i) {
						HistResultList hr = hrlist2.get(i);
						for (HistResult h : hr.histResults) {
							sum += h.mDistinctiveness;
							count++;
						}
					}

					sum /= count;
					quart = sum * mod;
					high = sum + quart;
					count = 0;

					for (i = 0; i < hrlist2.size(); ++i) {
						HistResultList hr = hrlist2.get(i);
						sum = 0.0;
						for (HistResult h : hr.histResults) {
							sum += h.mDistinctiveness;
						}
						sum /= hr.histResults.size();

						if (sum < high) {
							count++;
						}
					}
				} while (hrlist2.size() - count < 2500 && hrlist2.size() > 2500);

				for (i = 0; i < hrlist2.size(); ++i) {
					HistResultList hr = hrlist2.get(i);
					sum = 0.0;
					for (HistResult h : hr.histResults) {
						sum += h.mDistinctiveness;
					}
					sum /= hr.histResults.size();

					if (sum < high) {
						hrlist2.remove(i);
						--i;
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

				for (i = 0; i < hrlist.size(); ++i) {
					list1 = hrlist.get(i);

					for (int n = 0; n < list1.histResults.size(); ++n) {
						if (list1.histResults.get(n).mLongestSequence > 70) {
							hrlist.remove(i);
							i--;
							break;
						}
					}
				}

				for (i = 0; i < hrlist2.size(); ++i) {
					list1 = hrlist2.get(i);

					for (int n = 0; n < list1.histResults.size(); ++n) {
						if (list1.histResults.get(n).mLongestSequence > 70) {
							hrlist2.remove(i);
							i--;
							break;
						}
					}
				}

				System.out.println("Histogram result counts: " + hrlist.size() + ", " + hrlist2.size());

				for (HistResultList hr : hrlist) {
					for (HistResultList hr2 : hrlist2) {

						lowestDistance = 99999;
						compareIndex = 0;
						compareIndexMatch = 0;

						for (int[] ar : compareIndexArray) {
							compareIndex++;

							distanceFinal = 0;

							for (hri = 0; hri < hr.histResults.size(); ++hri) {
								result1 = hr.histResults.get(hri);
								distancesFirst = result1.getDistances();
								dist21 = result1.getDistances2();

								binDistance = 0;
								result2 = hr2.histResults.get(ar[hri]);

								if (result1.mDistinctiveness < result2.mMinDistinctiveness
										|| result1.mDistinctiveness > result2.mMaxDistinctiveness) {
									distanceFinal = 99999;
									break;
								}

								distancesSecond = result2.getDistances();
								dist22 = result2.getDistances2();

								for (i = 0; i < distancesFirst.length; ++i) {
									val = distancesFirst[i];
									val2 = distancesSecond[i];

									if (Math.abs(val2 - val) >= binThreshold2Negation) {
										binDistance += 2;
									} 

									if (binDistance >= binThreshold) {
										break;
									}
								}

								for (i = 0; i < dist21.length && binDistance < binThreshold; ++i) {
									val = dist21[i];
									val2 = dist22[i];

									if (Math.abs(val2 - val) >= binThreshold2Negation) {
										binDistance += 2;
									} 

									if (binDistance >= binThreshold) {
										break;
									}
								}

								distanceFinal += binDistance;

								if (distanceFinal >= binThreshold) {
									break;
								}
							}

							if (lowestDistance > distanceFinal) {
								compareIndexMatch = compareIndex - 1;
								lowestDistance = distanceFinal;
							}
						}

						distanceFinal = lowestDistance;

						if (distanceFinal < binThreshold) {
							FeatureMatch f = new FeatureMatch(hr.histResults.get(0).getX(),
									hr.histResults.get(0).getY(), hr2.histResults.get(0).getX(),
									hr2.histResults.get(0).getY());
							f.setRoughBinDistance(distanceFinal); // roughBinDistance);
							f.rotationArrayIndex = compareIndexMatch;

							featureMatches.add(f);

							g2d.setColor(Color.RED);
							// g2d.drawString(String.valueOf(hr.histResults.get(0).mDistinctiveness),
							// hr.histResults.get(0).getX() + 10, hr2.histResults.get(0).getY());
							// g2d.drawString(String.valueOf(hr2.histResults.get(0).mDistinctiveness),
							// hr2.histResults.get(0).getX() + 10 + width, hr2.histResults.get(0).getY());
						}
					}

					matchingIndex++;

					if (matchingIndex % 1000 == 0) {
						System.out.println("Features compared: " + matchingIndex + "/" + hrlist.size());
					}
				}

				rotationIndexMap.clear();

				for (i = 0; i < featureMatches.size(); ++i) {
					if (rotationIndexMap.containsKey(featureMatches.get(i).rotationArrayIndex)) {
						rotationIndexMap.replace(featureMatches.get(i).rotationArrayIndex,
								rotationIndexMap.get(featureMatches.get(i).rotationArrayIndex) + 1);
					} else {
						rotationIndexMap.put(featureMatches.get(i).rotationArrayIndex, 1);
					}
				}

				maxKey = 0;
				maxValue = 0;

				for (Map.Entry<Integer, Integer> e : rotationIndexMap.entrySet()) {
					if (maxValue < e.getValue()) {
						maxKey = e.getKey();
						maxValue = e.getValue();
					}
				}

				for (i = 0; i < featureMatches.size(); ++i) {
					if (featureMatches.get(i).rotationArrayIndex != maxKey) {
						featureMatches.remove(i);
						i--;
					}
				}

				Collections.sort(featureMatches, (o1, o2) -> o1.mRoughBinDistance - o2.mRoughBinDistance);

				int fmi = featureMatches.size() - 1;

				while (featureMatches.size() > 25 && fmi >= 0) {
					FeatureMatch fm = featureMatches.get(fmi);

					int x = fm.getX1();
					int y = fm.getY1();

					for (int io = 0; io < featureMatches.size(); ++io) {
						if (io == fmi)
							continue;

						FeatureMatch fm2 = featureMatches.get(io);

						if (fm2.getX1() >= (x - 15) && fm2.getX1() <= (x + 15)) {
							if (fm2.getY1() >= (y - 15) && fm2.getY1() <= (y + 15)) {
								featureMatches.remove(io);

								if (fmi > io) {
									fmi--;
								}

								io--;

								if (featureMatches.size() == 25) {
									fmi = -1;
									break;
								}
							}
						}
					}

					fmi--;
				}
				while (featureMatches.size() > 25) {
					featureMatches.remove(featureMatches.size() - 1);
				}

				System.out.println("Feature matching done.");
			} while (featureMatches.size() < 5 || evaluateFeatureMatchCloseness(featureMatches, width, height) < 0.007);
		} while ((featureMatches.size() < 5 || evaluateFeatureMatchCloseness(featureMatches, width, height) < 0.007)
				&& binThreshold < 56);

		TimeData.binDistanceUsage[binMergeCount - 1]++;

		long estimatedTime = System.currentTimeMillis() - startTime;
		TimeData.matching += estimatedTime;
		TimeData.matchingTimes.add(estimatedTime);

		System.out.println("Finalizing montage...");

		PrintWriter writer = new PrintWriter("moravec3_" + String.valueOf(fileIndex) + ".txt", "UTF-8");
		writer.println(file1);
		writer.println(file2);

		TimeData.featureMatches += featureMatches.size();

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

		writer.flush();
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

	public static double evaluateFeatureMatchCloseness(final List<FeatureMatch> featureMatches, int width, int height) {
		System.out.println("Evaluating feature match closeness...");

		int minx = Integer.MAX_VALUE, maxx = 0, miny = Integer.MAX_VALUE, maxy = 0;

		for (int i = 0; i < featureMatches.size(); ++i) {
			final FeatureMatch fm = featureMatches.get(i);

			int x = fm.getX1();
			int y = fm.getY1();

			if (minx > x) {
				minx = x;
			}

			if (miny > y) {
				miny = y;
			}

			if (maxx < x) {
				maxx = x;
			}

			if (maxy < y) {
				maxy = y;
			}
		}

		int len1 = maxx - minx;
		int len2 = maxy - miny;

		int area = len1 * len2;
		int area2 = width * height;

		double matchPercent = (double) area / (double) area2;

		System.out.println("Feature match closeness: " + matchPercent);

		return matchPercent;
	}

	public static void main(String[] args) throws IOException {
		final String[] files1 = { "paris_invalides_000662_sm_1.jpg", "paris_invalides_000662_sm_2.jpg",
				"paris_invalides_000662_sm_3.jpg", "paris_invalides_000662_sm_4.jpg", "paris_invalides_000662_sm_5.jpg",
				"paris_invalides_000662_sm_6.jpg", "paris_invalides_000662_sm_7.jpg", "paris_invalides_000662_sm_8.jpg",
				"paris_invalides_000662_sm_9.jpg", "paris_invalides_000662_sm_10.jpg",
				"paris_invalides_000662_pt_1.png", "paris_invalides_000662_pt_2.png", "paris_invalides_000662_pt_3.png",
				"paris_invalides_000662_pt_4.png", "paris_invalides_000662_pt_5.png", "paris_invalides_000662_pt_6.png",
				"paris_invalides_000662_pt_7.png", "paris_invalides_000662_pt_8.png", "paris_invalides_000662_pt_9.png",
				"paris_invalides_000662_pt_10.png", "paris_invalides_000662_pt_11.png",
				"paris_invalides_000662_pt_12.png", "paris_invalides_000662_lt_1.jpg",
				"paris_invalides_000662_lt_2.jpg", "paris_invalides_000662_lt_3.jpg", "paris_invalides_000662_lt_4.jpg",
				"paris_invalides_000662_lt_5.jpg", "paris_invalides_000662_lt_6.jpg", "paris_invalides_000662_lt_7.jpg",
				"paris_invalides_000662_lt_8.jpg", "paris_invalides_000662_lt_9.jpg", "paris_invalides_000662_1.jpg",
				"paris_invalides_000662_2.jpg", "paris_invalides_000662_3.jpg", "paris_invalides_000662_4.jpg",
				"paris_invalides_000662_5.jpg", "paris_invalides_000662_6.jpg", "paris_invalides_000662_7.jpg",
				"paris_invalides_000662_8.jpg", "paris_invalides_000662_9.jpg", "paris_invalides_000664.jpg",
				"paris_invalides_000665.jpg", "paris_invalides_000666.jpg", "paris_invalides_000667.jpg",
				"paris_invalides_000668.jpg", "paris_invalides_000669.jpg", "paris_invalides_000670.jpg",
				"paris_invalides_000671.jpg", "paris_invalides_000672.jpg", "paris_invalides_000673.jpg",
				"paris_invalides_000662.jpg", "all_souls_000065.jpg", "all_souls_000065.jpg", "pano8.jpg", "pano6.jpg",
				"1Hill.JPG", "2Hill.JPG", "S3.jpg", "b.jpg", "P1011370.JPG", "P1011069.JPG", "P1010372.JPG",
				"grail03.jpg", "DSC_0178.jpg", "bike1.png", "Yosemite1.jpg", "img2.png", "h1.jpg", "base1.jpg",
				"Test1025.jpg", "Test1027.jpg", "Test81.jpg", "Test1027.jpg", "Test1025.jpg", "Test81.jpg",
				"Test1025.jpg", "Test72.jpg", "Test65.jpg", "Test21.jpg", "Test1027.jpg", "Test1027.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test81.jpg", "Test81.jpg", "Test3000_rot.jpg", "Test47_rot.jpg",
				"Test3030.jpg", "Test1031.jpg", "Test1027.jpg", "Test1025.jpg", "Test1024.jpg", "Test506.jpg",
				"Test506.jpg", "Test404.jpg", "Test705.jpg", "Test705.jpg", "Test766.jpg", "Test766.jpg", "Test82.jpg",
				"Test5000_rot.jpg", "Test3000_rot.jpg", "Test1500.jpg", "Test1310_rot.PNG", "Test1199_rot.PNG",
				"Test1000_rot.jpg", "Test2120_rot.jpg", "Test1999_rot.jpg", "Test4.jpg", "Test6.jpg", "Test21.jpg",
				"Test34_rot.jpg", "Test37.jpg", "Test47_rot.jpg", "Test48_rot.png", "Test65.jpg", "Test70.jpg",
				"Test99.jpg", "Test120_rot.jpeg", "Test121_rot.png", "Test122_rot.png", "Test123_rot.jpg",
				"Test200_rot.jpg", "Test211_rot.jpg", "Test240.jpg", "Test300.jpg", "Test400.jpg", "Test600.jpg",
				"Test800.jpg" };
		final String[] files2 = { "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "all_souls_000051.jpg", "all_souls_000066.jpg", "pano9.jpg", "pano7.jpg",
				"2Hill.JPG", "3Hill.JPG", "S5.jpg", "c.jpg", "P1011371.JPG", "P1011070.JPG", "P1010373.JPG",
				"grail04.jpg", "DSC_0179.jpg", "bike2.png", "Yosemite2.jpg", "img3.png", "h2.jpg", "base2.jpg",
				"Test1026_4.jpg", "Test1028_3.jpg", "Test85.jpg", "Test1028_2.jpg", "Test1026_3.jpg", "Test82_2.jpg",
				"Test1026_2.jpg", "Test70.jpg", "Test67.jpg", "Test23.jpg", "Test1029.jpg", "Test1030.jpg",
				"Test1502.jpg", "Test1503.jpg", "Test83.jpg", "Test84.jpg", "Test3002_rot.jpg", "Test49_rot.jpg",
				"Test3031.jpg", "Test1032.jpg", "Test1028.jpg", "Test1026.jpg", "Test1023.jpg", "Test507.jpg",
				"Test508.jpg", "Test405.jpg", "Test706.jpg", "Test707.jpg", "Test767.jpg", "Test768.jpg", "Test81.jpg",
				"Test5001_rot.jpg", "Test3001_rot.jpg", "Test1501.jpg", "Test1311_rot.PNG", "Test1200_rot.PNG",
				"Test1001_rot.jpg", "Test2121_rot.jpg", "Test2000_rot.jpg", "Test5.jpg", "Test7.jpg", "Test22.jpg",
				"Test35_rot.jpg", "Test38.jpg", "Test48_rot.jpg", "Test49_rot.png", "Test66.jpg", "Test71.jpg",
				"Test100.jpg", "Test124_rot.jpeg", "Test125_rot.png", "Test126_rot.png", "Test127_rot.jpg",
				"Test201_rot.jpg", "Test212_rot.jpg", "Test241.jpg", "Test310.jpg", "Test410.jpg", "Test610.jpg",
				"Test810.jpg" };

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

		System.out.println("Feature matches: " + TimeData.featureMatches);
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
		final List<Double> newList = new ArrayList<>();

		if (values.size() == 1) {
			return newList;
		}

		double mean = getMean(values);
		double stdDev = getStdDev(values);

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
