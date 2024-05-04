package coifv5;

import java.util.ArrayList;
import java.util.Arrays;
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
		int[] hist2 = new int[256];
		int[] hist3 = new int[256];
		Arrays.fill(hist, 0);
		Arrays.fill(hist2, 0);
		Arrays.fill(hist3, 0);

		int y, dx, dy, distanceSquared, val;
		
		for (int x = Math.max(0, circleX - radius - 3); x < Math.min(width, circleX + radius + 3); x++) {
		    for (y = Math.max(0, circleY - radius - 3); y < Math.min(height, circleY + radius + 3); y++) {
				dx = x - circleX;
				dy = y - circleY;
				distanceSquared = dx * dx + dy * dy;

				if (distanceSquared <= radiusSquared) {
					val = image[x][y];
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

		int x, y;
		CircleResult cr, crSec, crThird, crFourth;
		HistResult hr, hrSec, hrThird, hrFth;
		int[] hist, hist2, center, histSec, hist2Sec, centerSec, histThird, hist2Third, centerThird, histFourth, hist2Fourth, centerFourth;
		
		for (MoravecResult mr : results) {
			x = mr.getX();
			y = mr.getY();

			if (x - 4 >= 0 && y - 4 >= 0 && x + 4 < width && y + 4 < height) {
				cr = circles3(image, mr.getX(), mr.getY() - 4, radius);
				hist = cr.getHist();
				hist2 = cr.getInnerHist();
				center = cr.getCenterHist();

				hr = new HistResult(hist, hist2, mr.getX(), mr.getY() - 4, center);
				hr.computeDistances(binMergeCount);
				hr.computeDistinctiveness(2);

				crSec = circles3(image, mr.getX() - 4, mr.getY(), radius);
				histSec = crSec.getHist();
				hist2Sec = crSec.getInnerHist();
				centerSec = crSec.getCenterHist();

				hrSec = new HistResult(histSec, hist2Sec, mr.getX() - 4, mr.getY(), centerSec);
				hrSec.computeDistances(binMergeCount);
				hrSec.computeDistinctiveness(2);

				crThird = circles3(image, mr.getX() + 4, mr.getY(), radius);
				histThird = crThird.getHist();
				hist2Third = crThird.getInnerHist();
				centerThird = crThird.getCenterHist();

				hrThird = new HistResult(histThird, hist2Third, mr.getX() + 4, mr.getY(), centerThird);
				hrThird.computeDistances(binMergeCount);
				hrThird.computeDistinctiveness(2);

				crFourth = circles3(image, mr.getX(), mr.getY() + 4, radius);
				histFourth = crFourth.getHist();
				hist2Fourth = crFourth.getInnerHist();
				centerFourth = crFourth.getCenterHist();

				hrFth = new HistResult(histFourth, hist2Fourth, mr.getX(), mr.getY() + 4, centerFourth);
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
