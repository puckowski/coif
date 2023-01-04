
public class HistResult {

	private int[] mHist;
	private int[] mInnerHist;
	private int[] mCenterHist;
	private int mX;
	private int mY;
	private int[] mDistances;
	private int[] mDistances2;
	private int mSsd;

	public int mDistinctiveness;
	public int mMinDistinctiveness;
	public int mMaxDistinctiveness;
	
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

	public int getSsd() {
		return mSsd;
	}

	public void setSsd(int ssd) {
		mSsd = ssd;
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
		mMinDistinctiveness = mDistinctiveness - 10;
		mMaxDistinctiveness = mDistinctiveness + 10;
	}

	public void computeDistances(int mergeBinCount) {
		final int histLength = 256 / mergeBinCount;
		
		mDistances = new int[histLength];
		for (int i = 0; i < histLength; ++i) {
			mDistances[i] = 0;
		}

		int sum1 = 0;
		int sum2 = 0;
		int binIndex = 0;
		int angleIndex = 0;

		for (int i = 0; i < 256; ++i) {
			sum1 += mHist[i];
			sum2 += mInnerHist[i];
			binIndex++;

			if (binIndex == mergeBinCount) {
				mDistances[angleIndex] = Math.abs(sum1 - sum2);
				angleIndex++;
				binIndex = 0;
			}
		}
		
		mDistances2 = new int[histLength];
		for (int i = 0; i < histLength; ++i) {
			mDistances2[i] = 0;
		}

		sum1 = 0;
		sum2 = 0;
		binIndex = 0;
		angleIndex = 0;

		for (int i = 0; i < 256; ++i) {
			sum1 += mHist[i];
			sum2 += mCenterHist[i];
			binIndex++;

			if (binIndex == mergeBinCount) {
				mDistances2[angleIndex] = Math.abs(sum1 - sum2);
				angleIndex++;
				binIndex = 0;
			}
		}
	}
}
