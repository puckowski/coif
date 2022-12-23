import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoravecResult {

	private double mSsdRight;
	private double mSsdBottomRight;
	private double mSsdBottom;
	private double mSsdBottomLeft;
	private double mSsdTopRight;
	private double mSsdTopLeft;
	private double mSsdTop;
	private double mSsdLeft;
	private double mMinSsd;
	private int mX;
	private int mY;

	public int emd;

	public MoravecResult() {

	}

	public MoravecResult(double ssdRight, double ssdBottomRight, double ssdBottom, double ssdBottomLeft,
			double ssdTopRight, double ssdTopLeft, double ssdTop, double ssdLeft, int x, int y) {
		mSsdRight = ssdRight;
		mSsdBottomRight = ssdBottomRight;
		mSsdBottom = ssdBottom;
		mSsdBottomLeft = ssdBottomLeft;
		mSsdTopRight = ssdTopRight;
		mSsdTopLeft = ssdTopLeft;
		mSsdTop = ssdTop;
		mSsdLeft = ssdLeft;
		mX = x;
		mY = y;

		List<Double> values = new ArrayList<Double>();
		values.add(ssdRight);
		values.add(ssdBottomRight);
		values.add(ssdBottom);
		values.add(ssdBottomLeft);
		values.add(ssdTopRight);
		values.add(ssdTopLeft);
		values.add(ssdTop);
		values.add(ssdLeft);

		mMinSsd = Collections.min(values);
	}

	public double getSsdRight() {
		return mSsdRight;
	}

	public double getSsdBottomRight() {
		return mSsdBottomRight;
	}

	public double getSsdBottom() {
		return mSsdBottom;
	}

	public double getSsdBottomLeft() {
		return mSsdBottomLeft;
	}

	public double getSsdTopRight() {
		return mSsdTopRight;
	}

	public double getSsdTopLeft() {
		return mSsdTopLeft;
	}

	public double getSsdTop() {
		return mSsdTop;
	}

	public double getSsdLeft() {
		return mSsdLeft;
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
