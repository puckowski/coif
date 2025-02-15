package coifv7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	public static CircleResult circles3(int[][] image, final int circleX, final int circleY, final int radius) {
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

		int x, y;
		CircleResult cr, crSec, crThird, crFourth;
		HistResult hr, hrSec, hrThird, hrFth;
		int[] hist, hist2, center, histSec, hist2Sec, centerSec, histThird, hist2Third, centerThird, histFourth, hist2Fourth, centerFourth;
		Map<HistResultList, HistResult> all = new HashMap<>();

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

				all.put(histResultList, histResultList.histResults.get(0));
				
				resultList.add(histResultList);
			}
		}

		Map<HistResultList, HistResult> filtered = HistResultFilter.filterHistResults(all, 15, 50);
		// 20, 1 also tried; worse than 50, 1
		// 15, 1 also tried; worse than 20, 1
		System.err.println("#####: " + all.size() + " " + filtered.size());
		
		return new ArrayList<>(filtered.keySet());
	}
}
