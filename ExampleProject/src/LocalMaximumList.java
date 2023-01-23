import java.util.PriorityQueue;
import java.util.Queue;

public class LocalMaximumList {
	private int mStartX;
	private int mStartY;

	private int mFinalX;
	private int mFinalY;

	private int mLocalFeatureCount;

	private Queue<MoravecResult> mResults;
	
	public LocalMaximumList(int startX, int startY, int finalX, int finalY, int localFeatureCount) {
		mStartX = startX;
		mStartY = startY;
		mFinalX = finalX;
		mFinalY = finalY;

		mLocalFeatureCount = localFeatureCount;

		mResults = new PriorityQueue<MoravecResult>(localFeatureCount + 1,
				(firstResult, secondResult) -> Double.compare(firstResult.getMinSsd(), secondResult.getMinSsd()));
	}

	public boolean containsPoint(int x, int y) {
		if (x >= mStartX && x <= mFinalX) {
			if (y >= mStartY && y <= mFinalY) {
				return true;
			}
		}

		return false;
	}

	public Queue<MoravecResult> getResults() {
		return mResults;
	}

	public void addResult(MoravecResult result) {
		mResults.add(result);

		if (mResults.size() > mLocalFeatureCount) {
			mResults.remove();
		}
	}

	@Override
	public String toString() {
		return "(" + mStartX + ", " + mStartY + ") (" + mFinalX + ", " + mFinalY + ")";
	}
}
