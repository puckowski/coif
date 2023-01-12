package coifv4;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

		startTime = System.currentTimeMillis();

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

				LocalMaximumList newList = new LocalMaximumList(x, y, targetEndX, targetEndY, mLocalFeatureCount);
				localMaximumLists.add(newList);
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

		estimatedTime = System.currentTimeMillis() - startTime;
		TimeData.moravec += estimatedTime;
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
