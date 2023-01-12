package coifv4;

public class FeatureMatch {

	public int mX1;
	public int mY1;

	public int mX2;
	public int mY2;
	
	public int roughBinDistance;

	public FeatureMatch(int x1, int y1, int x2, int y2) {
		mX1 = x1;
		mY1 = y1;

		mX2 = x2;
		mY2 = y2;
	}

	public int getX1() {
		return mX1;
	}

	public int getY1() {
		return mY1;
	}

	public int getX2() {
		return mX2;
	}

	public int getY2() {
		return mY2;
	}
}
