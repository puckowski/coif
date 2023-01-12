package coifv4;

public class MoravecResult {

	private double mMinSsd;
	private int mX;
	private int mY;

	public MoravecResult() {

	}

	public MoravecResult(double ssd, int x, int y) {
		mX = x;
		mY = y;
		mMinSsd = ssd;
	}

	public double getMinSsd() {
		return mMinSsd;
	}

	public void setX(int x) {
		mX = x;
	}

	public void setY(int y) {
		mY = y;
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	@Override
	public String toString() {
		return "(" + mX + ", " + mY + ") " + String.valueOf(mMinSsd);
	}
}
