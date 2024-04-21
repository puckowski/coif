package coifv6;


public class CircleResult {

	private int[] mHist;
	private int[] mInnerHist;
	private int[] mCenterHist;

	public CircleResult(int[] hist, int[] innerHist, int[] centerHist) {
		mHist = hist;
		mInnerHist = innerHist;
		mCenterHist = centerHist;
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
}
