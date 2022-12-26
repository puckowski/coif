
public class CircleResult {

	private int[] mHist;
	private int[] mInnerHist;

	public CircleResult(int[] hist, int[] innerHist) {
		mHist = hist;
		mInnerHist = innerHist;
	}

	public int[] getHist() {
		return mHist;
	}

	public int[] getInnerHist() {
		return mInnerHist;
	}
}
