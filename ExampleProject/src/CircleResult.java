
public class CircleResult {

	private int[] mHist;
	private int[] mInnerHist;
	private int[] mInnermostHist;

	public CircleResult(int[] hist, int[] innerHist, int[] innermostHist) {
		mHist = hist;
		mInnerHist = innerHist;
		mInnermostHist = innermostHist;
	}

	public int[] getHist() {
		return mHist;
	}

	public int[] getInnerHist() {
		return mInnerHist;
	}

	public int[] getInnermostHist() {
		return mInnermostHist;
	}
}
