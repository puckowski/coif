import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CircleBuilderCallable implements Callable {

	private int[][] image;
	private int radius;
	private List<MoravecResult> results;
	private int binMergeCount;
	
	public CircleBuilderCallable(int[][] image, int radius, List<MoravecResult> results,
			final int binMergeCount) {
		this.image = image;
		this.radius = radius;
		this.results = results;
		this.binMergeCount = binMergeCount;
	}
	
	public CircleResult circles3(int[][] image, final int circleX, final int circleY, final int radius) {
		final int width = image.length;
		final int height = image[0].length;
		final int radiusSquared = radius * radius;
		final int radiusSquaredHalf = radiusSquared / 3;

		final int radiusSquaredHalf2 = radiusSquared / 7;

		int[] hist = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist[i] = 0;
		}

		int[] hist2 = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist2[i] = 0;
		}

		int[] hist3 = new int[256];
		for (int i = 0; i < hist.length; ++i) {
			hist3[i] = 0;
		}

		for (int x = circleX - radius - 3; x < circleX + radius + 3; x++) {
			if (x < 0 || x >= width)
				continue;

			for (int y = circleY - radius - 3; y < circleY + radius + 3; y++) {
				if (y < 0 || y >= height)
					continue;

				double dx = x - circleX;
				double dy = y - circleY;
				double distanceSquared = dx * dx + dy * dy;

				if (distanceSquared <= radiusSquared) {
					int val = image[x][y];
					hist[val]++;

					if (distanceSquared <= (radiusSquaredHalf)) {
						hist2[val]++;

						if (distanceSquared <= (radiusSquaredHalf2)) {
							hist3[val]++;
						}
					}
				}
			}
		}

		CircleResult circleResult = new CircleResult(hist, hist2, hist3);

		return circleResult;
	}

	@Override
	public List<HistResultList> call() throws Exception {
		final List<HistResultList> resultList = new ArrayList<>();
		final int width = image.length;
		final int height = image[0].length;

		for (MoravecResult mr : results) {
			int x = mr.getX();
			int y = mr.getY();

			if (x - 4 >= 0 && y - 4 >= 0 && x + 4 < width && y + 4 < height) {
				CircleResult cr = circles3(image, mr.getX(), mr.getY() - 4, radius);
				int[] hist = cr.getHist();
				int[] hist2 = cr.getInnerHist();
				int[] center = cr.getCenterHist();

				HistResult hr = new HistResult(hist, hist2, mr.getX(), mr.getY() - 4, center);
				hr.computeDistances(binMergeCount);
				hr.computeDistinctiveness(2);

				CircleResult crSec = circles3(image, mr.getX() - 4, mr.getY(), radius);
				int[] histSec = crSec.getHist();
				int[] hist2Sec = crSec.getInnerHist();
				int[] centerSec = crSec.getCenterHist();

				HistResult hrSec = new HistResult(histSec, hist2Sec, mr.getX() - 4, mr.getY(), centerSec);
				hrSec.computeDistances(binMergeCount);
				hrSec.computeDistinctiveness(2);

				CircleResult crThird = circles3(image, mr.getX() + 4, mr.getY(), radius);
				int[] histThird = crThird.getHist();
				int[] hist2Third = crThird.getInnerHist();
				int[] centerThird = crThird.getCenterHist();

				HistResult hrThird = new HistResult(histThird, hist2Third, mr.getX() + 4, mr.getY(), centerThird);
				hrThird.computeDistances(binMergeCount);
				hrThird.computeDistinctiveness(2);

				CircleResult crFourth = circles3(image, mr.getX(), mr.getY() + 4, radius);
				int[] histFourth = crFourth.getHist();
				int[] hist2Fourth = crFourth.getInnerHist();
				int[] centerFourth = crFourth.getCenterHist();

				HistResult hrFth = new HistResult(histFourth, hist2Fourth, mr.getX(), mr.getY() + 4, centerFourth);
				hrFth.computeDistances(binMergeCount);
				hrFth.computeDistinctiveness(2);

				HistResultList histResultList = new HistResultList();
				histResultList.histResults.add(hr);
				histResultList.histResults.add(hrSec);
				histResultList.histResults.add(hrThird);
				histResultList.histResults.add(hrFth);

				resultList.add(histResultList);
			}
		}

		return resultList;
	}
}
