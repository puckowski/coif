package coifv7;

public class HistResult {

	private int[] mHist;
	private int[] mInnerHist;
	private int[] mCenterHist;
	private int mX;
	private int mY;
	private int[] mDistances;
	private int[] mDistances2;

	public int mDistinctiveness;
	public int mMinDistinctiveness;
	public int mMaxDistinctiveness;

	public int mLongestSequence;

	public HistResult(int[] hist, int[] innerHist, int x, int y, int[] centerHist) {
		mHist = hist;
		mInnerHist = innerHist;
		mCenterHist = centerHist;
		mX = x;
		mY = y;

		mDistinctiveness = 0;
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public int[] getHist() {
		return mHist;
	}

	public int[] getInnerHist() {
		return mInnerHist;
	}

	public int[] getCenterHist() {
		return mCenterHist;
	}

	public int[] getDistances() {
		return mDistances;
	}

	public int[] getDistances2() {
		return mDistances2;
	}

	public void computeDistinctiveness(final int modifier) {
		int score = 0;
		for (int n = 0; n < mHist.length; ++n) {
			if (mHist[n] < modifier)
				score++;
		}

		mDistinctiveness = 256 - score;
		mMinDistinctiveness = mDistinctiveness - 10;// 2; // or 4 or 10;
		mMaxDistinctiveness = mDistinctiveness + 10;//2; // or 4 or 10;

		int longestSequence = 0;
		int count = 0;
		for (int n = 0; n < mHist.length; ++n) {
			if (mHist[n] < 25) {
				count++;
			} else {
				if (longestSequence < count) {
					longestSequence = count;
				}

				count = 0;
			}
		}

		mLongestSequence = longestSequence;
	}

	public void computeDistances(int mergeBinCount) {
		final int histLength = 256 / mergeBinCount;

		mDistances = new int[histLength];

		int sum1 = 0;
		int sum2 = 0;
		int binIndex = 0;
		int angleIndex = 0;

		for (int i = 0; i < 256; ++i) {
			sum1 += mHist[i];
			sum2 += mInnerHist[i];
			binIndex++;

			if (binIndex == mergeBinCount) {
				mDistances[angleIndex] = sum1 - sum2;// sum1 - sum2;Math.abs(sum1 - sum2);
				angleIndex++;
				binIndex = 0;
			}
		}

		mDistances2 = new int[histLength];

		sum1 = 0;
		sum2 = 0;
		binIndex = 0;
		angleIndex = 0;

		for (int i = 0; i < 256; ++i) {
			sum1 += mInnerHist[i];
			sum2 += mCenterHist[i];
			binIndex++;

			if (binIndex == mergeBinCount) {
				mDistances2[angleIndex] = sum1 - sum2;// Math.abs(sum1 - sum2);
				angleIndex++;
				binIndex = 0;
			}
		}
	}
}
