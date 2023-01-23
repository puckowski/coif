import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoravecProcessor {

	private int mBaseThreshold;
	private int mLocalFeatureCount;
	private double mLocalFeaturePercentage;

	private List<MoravecResult> mResults;
	private int[][] mGrayscaleData;

	public MoravecProcessor() {
		mBaseThreshold = 131072;
		mLocalFeatureCount = 10;
		mLocalFeaturePercentage = 0.02;
	}

	public MoravecProcessor(int baseThreshold, int localFeatureCount, double localFeaturePercentage) {
		mBaseThreshold = baseThreshold;
		mLocalFeatureCount = localFeatureCount;
		mLocalFeaturePercentage = localFeaturePercentage;
	}

	public void process(final String absoluteFileName) throws IOException {
		long startTime = System.currentTimeMillis();

		mResults = new ArrayList<MoravecResult>();

		ImageUtils imageUtils = new ImageUtils();
		mGrayscaleData = imageUtils.localGrayscaleArray(absoluteFileName);

		if (mGrayscaleData.length == 0) {
			System.out.println("Grayscale data invalid.");

			return;
		}

		long estimatedTime = System.currentTimeMillis() - startTime;
		TimeData.imageLoad += estimatedTime;
	}

	public void average(int[][] otherData) {
		double avg = 0.0;
		long count = 0;
		for (int i = 0; i < otherData.length; ++i) {
			for (int n = 0; n < otherData[i].length; ++n) {
				avg += otherData[i][n];
				count++;
			}
		}
		avg /= count;

		double avg2 = 0.0;
		long count2 = 0;
		for (int i = 0; i < mGrayscaleData.length; ++i) {
			for (int n = 0; n < mGrayscaleData[i].length; ++n) {
				avg2 += mGrayscaleData[i][n];
				count2++;
			}
		}
		avg2 /= count2;

		System.out.println(avg + " " + avg2);

		int iters = 0;

		while (avg2 > (avg * 1.005) && iters < 100) {
			for (int i = 0; i < mGrayscaleData.length; ++i) {
				for (int n = 0; n < mGrayscaleData[i].length; ++n) {
					mGrayscaleData[i][n]--;

					if (mGrayscaleData[i][n] < 0) {
						mGrayscaleData[i][n] = 0;
					}
				}
			}

			avg2 = 0.0;
			count2 = 0;
			for (int i = 0; i < mGrayscaleData.length; ++i) {
				for (int n = 0; n < mGrayscaleData[i].length; ++n) {
					avg2 += mGrayscaleData[i][n];
					count2++;
				}
			}
			avg2 /= count2;

			System.out.println("GT: " + avg + " " + avg2);

			iters++;
		}

		while (avg2 < (avg * 0.995) && iters < 100) {
			for (int i = 0; i < mGrayscaleData.length; ++i) {
				for (int n = 0; n < mGrayscaleData[i].length; ++n) {
					mGrayscaleData[i][n]++;

					if (mGrayscaleData[i][n] > 255) {
						mGrayscaleData[i][n] = 255;
					}
				}
			}

			avg2 = 0.0;
			count2 = 0;
			for (int i = 0; i < mGrayscaleData.length; ++i) {
				for (int n = 0; n < mGrayscaleData[i].length; ++n) {
					avg2 += mGrayscaleData[i][n];
					count2++;
				}
			}
			avg2 /= count2;

			System.out.println("LT: " + avg + " " + avg2);

			iters++;
		}
	}

	public double log2(double N) {
		double result = (Math.log(N) / Math.log(2));

		return result;
	}

	public double getShannonEntropyImage(int x, int y, int endX, int endY) {
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (endX >= mGrayscaleData.length)
			endX = mGrayscaleData.length - 1;
		if (endY >= mGrayscaleData[0].length)
			endY = mGrayscaleData[0].length - 1;

		List<String> values = new ArrayList<String>();
		int n = 0;
		Map<Integer, Integer> occ = new HashMap<>();
		for (int i = y; i < endY; i++) {
			for (int j = x; j < endX; j++) {
				int pixel = mGrayscaleData[j][i];

				if (!values.contains(String.valueOf(pixel)))
					values.add(String.valueOf(pixel));
				if (occ.containsKey(pixel)) {
					occ.put(pixel, occ.get(pixel) + 1);
				} else {
					occ.put(pixel, 1);
				}
				++n;
			}
		}
		double e = 0.0;
		for (Map.Entry<Integer, Integer> entry : occ.entrySet()) {
			double p = (double) entry.getValue() / n;
			e += p * log2(p);
		}
		return -e;
	}

	public void process2() {
		long startTime = System.currentTimeMillis();

		final int width = mGrayscaleData.length;
		final int height = mGrayscaleData[0].length;

		int localWidth = (int) Math.round(width * mLocalFeaturePercentage);

		List<LocalMaximumList> localMaximumLists = new ArrayList<LocalMaximumList>();

		for (int x = 0; x < width; x += localWidth) {
			for (int y = 0; y < height; y += localWidth) {
				int targetEndX = x + localWidth;
				int targetEndY = y + localWidth;

				if (targetEndX >= width) {
					targetEndX = width - 1;
				}

				if (targetEndY >= height) {
					targetEndY = height - 1;
				}

				double entropy = getShannonEntropyImage(x, y, targetEndX, targetEndY);

				if (entropy < 6) {
					LocalMaximumList newList = new LocalMaximumList(x, y, targetEndX, targetEndY, mLocalFeatureCount);
					localMaximumLists.add(newList);
				}
			}
		}

		int[][] xy_shifts = { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 } };

		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				int ssd = 999999999;

				for (int[] shift : xy_shifts) {
					int diff = mGrayscaleData[x + shift[0]][y + shift[1]];
					diff = diff - mGrayscaleData[x][y];
					diff = diff * diff;

					if (diff < ssd) {
						ssd = diff;
					}
				}

				if (ssd > mBaseThreshold) {
					MoravecResult result = new MoravecResult(ssd, x, y);

					LocalMaximumList matchingMaximumList = null;
					for (LocalMaximumList maximumList : localMaximumLists) {
						if (maximumList.containsPoint(x, y)) {
							matchingMaximumList = maximumList;
							break;
						}
					}

					if (matchingMaximumList != null) {
						matchingMaximumList.addResult(result);
					}
				}
			}
		}

		localMaximumLists.forEach(localMaximumList -> {
			mResults.addAll(localMaximumList.getResults());
		});

		long estimatedTime = System.currentTimeMillis() - startTime;
		TimeData.moravec += estimatedTime;
		TimeData.moravecTimes.add(estimatedTime);
	}

	public BufferedImage processWithMarkup(final String absoluteFileName) throws IOException {
		process(absoluteFileName);

		ImageUtils imageUtils = new ImageUtils();
		BufferedImage newImage = imageUtils.markupGrayscaleWithResults(mGrayscaleData, mResults);

		return newImage;
	}

	public List<MoravecResult> getResults() {
		return mResults;
	}

	public int[][] getGrayscaleData() {
		return mGrayscaleData;
	}

}
