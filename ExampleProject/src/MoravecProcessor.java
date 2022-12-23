import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MoravecProcessor {

	private int mPatchSize;
	private int mPatchSizeHalf;
	private int mBaseThreshold;
	private int mLocalFeatureCount;
	private double mLocalFeaturePercentage;

	private List<MoravecResult> mResults;
	private int[][] mGrayscaleData;

	public MoravecProcessor() {
		mPatchSize = 8;
		mPatchSizeHalf = mPatchSize / 2;
		mBaseThreshold = 131072;
		mLocalFeatureCount = 10;
		mLocalFeaturePercentage = 0.02;
	}

	public MoravecProcessor(int patchSize, int baseThreshold, int localFeatureCount, double localFeaturePercentage) {
		mPatchSize = patchSize;
		mPatchSizeHalf = mPatchSize / 2;
		mBaseThreshold = baseThreshold;
		mLocalFeatureCount = localFeatureCount;
		mLocalFeaturePercentage = localFeaturePercentage;
	}

	private boolean canProcessPatch(final int width, final int height, final int x, final int y, final int mPatchSize,
			final int mPatchSizeHalf, final int xOffset, final int yOffset) {
		int startX = x - mPatchSizeHalf + (xOffset);
		int endX = x + mPatchSizeHalf + (xOffset);

		int startY = y - mPatchSizeHalf + (yOffset);
		int endY = y + mPatchSizeHalf + (yOffset);

		if (startX >= 0 && endX < width) {
			if (startY >= 0 && endY < height) {
				return true;
			}
		}

		return false;
	}

	private int[][] getPatch(final int[][] input, final int width, final int height, final int x, final int y,
			final int mPatchSize, final int mPatchSizeHalf, final int xOffset, final int yOffset) {
		int[][] patch = new int[mPatchSize][mPatchSize];
		for (int i = 0; i < mPatchSize; ++i) {
			for (int n = 0; n < mPatchSize; ++n) {
				patch[i][n] = 0;
			}
		}

		int startX = x - mPatchSizeHalf + (xOffset);
		int endX = x + mPatchSizeHalf + (xOffset);

		int startY = y - mPatchSizeHalf + (yOffset);
		int endY = y + mPatchSizeHalf + (yOffset);

		if (startX >= 0 && endX < width) {
			if (startY >= 0 && endY < height) {
				for (int i = 0; i < mPatchSize; ++i) {
					for (int n = 0; n < mPatchSize; ++n) {
						int val = input[startX][startY];

						patch[i][n] = val;

						startX++;
					}

					startX -= mPatchSize;
					startY++;
				}
			}
		}

		return patch;
	}

	private int getSsdBetweenPatches(int[][] patchOne, int[][] patchTwo, int patchWidth) {
		int ssd = 0;

		for (int i = 0; i < patchWidth; ++i) {
			for (int n = 0; n < patchWidth; ++n) {
				int diff = patchOne[i][n] - patchTwo[i][n];

				ssd += diff * diff;
			}
		}

		return ssd;
	}

	public static Scanner scan = new Scanner(System.in);

	public void process(final String absoluteFileName) throws IOException {
		mResults = new ArrayList<MoravecResult>();

		ImageUtils imageUtils = new ImageUtils();
		mGrayscaleData = imageUtils.localGrayscaleArray(absoluteFileName);

		if (mGrayscaleData.length == 0) {
			System.out.println("Grayscale data invalid.");

			return;
		}

		final int width = mGrayscaleData.length;
		final int height = mGrayscaleData[0].length;

		final int localWidth = (int) Math.round(width * mLocalFeaturePercentage);
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

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				boolean canProcessCenter = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 0, 0);
				boolean canProcessRight = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 1, 0);
				boolean canProcessBottomRight = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 1, 1);
				boolean canProcessBottom = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 0, 1);
				boolean canProcessBottomLeft = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, -1, 1);
				boolean canProcessTopRight = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 1, -1);
				boolean canProcessTopLeft = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, -1, -1);
				boolean canProcessTop = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, 0, -1);
				boolean canProcessLeft = canProcessPatch(width, height, x, y, mPatchSize, mPatchSizeHalf, -1, 0);

				if (canProcessCenter && canProcessRight && canProcessBottomRight && canProcessBottom
						&& canProcessBottomLeft && canProcessTopRight && canProcessTopLeft && canProcessTop
						&& canProcessLeft) {
					int[][] patchCenter = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, 0,
							0);
					int[][] patchRight = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, 1,
							0);
					int[][] patchBottomRight = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf,
							1, 1);
					int[][] patchBottom = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, 0,
							1);
					int[][] patchBottomLeft = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf,
							-1, 1);
					int[][] patchTopRight = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, 1,
							-1);
					int[][] patchTopLeft = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, -1,
							-1);
					int[][] patchTop = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, 0, -1);
					int[][] patchLeft = getPatch(mGrayscaleData, width, height, x, y, mPatchSize, mPatchSizeHalf, -1,
							0);

					double ssdRight = getSsdBetweenPatches(patchCenter, patchRight, mPatchSize);
					double ssdBottomRight = getSsdBetweenPatches(patchCenter, patchBottomRight, mPatchSize);
					double ssdBottom = getSsdBetweenPatches(patchCenter, patchBottom, mPatchSize);
					double ssdBottomLeft = getSsdBetweenPatches(patchCenter, patchBottomLeft, mPatchSize);
					double ssdTopRight = getSsdBetweenPatches(patchCenter, patchTopRight, mPatchSize);
					double ssdTopLeft = getSsdBetweenPatches(patchCenter, patchTopLeft, mPatchSize);
					double ssdTop = getSsdBetweenPatches(patchCenter, patchTop, mPatchSize);
					double ssdLeft = getSsdBetweenPatches(patchCenter, patchLeft, mPatchSize);

					MoravecResult result = new MoravecResult(ssdRight, ssdBottomRight, ssdBottom, ssdBottomLeft,
							ssdTopRight, ssdTopLeft, ssdTop, ssdLeft, x, y);

					if (result.getMinSsd() > mBaseThreshold) {
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
		}

		localMaximumLists.forEach(localMaximumList -> {
			mResults.addAll(localMaximumList.getResults());
		});
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
