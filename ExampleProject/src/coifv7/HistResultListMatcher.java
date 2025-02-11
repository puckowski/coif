package coifv7;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class HistResultListMatcher implements Callable {

	private List<HistResultList> hrlist;
	private List<HistResultList> hrlist2;
	private Graphics2D g2d;
	private List<FeatureMatch> featureMatches = new ArrayList<FeatureMatch>();
	private int binThreshold2;
	private int binThreshold;

	public HistResultListMatcher(List<HistResultList> hrlist, List<HistResultList> hrlist2, Graphics2D g2d, int binThreshold2, int binThreshold) {
		this.hrlist = hrlist;
		this.hrlist2 = hrlist2;
		this.g2d = g2d;
		this.binThreshold2= binThreshold2;
		this.binThreshold = binThreshold;
	}

	public List<FeatureMatch> getMatches() {
		return featureMatches;
	}

	@Override
	public List<FeatureMatch> call() throws Exception {
		List<FeatureMatch> featureMatches = new ArrayList<FeatureMatch>();

		double binThreshold2Negation = binThreshold2 * 0.85;

		int matchingIndex = 0;
		int[] distancesFirst;
		int[] distancesSecond;
		int[] dist21;
		int[] dist22;
		int binDistance;

		double val, val2;
		int i;

		int[][] compareIndexArray = { { 0, 1, 2, 3 }, { 1, 2, 3, 0 }, { 2, 3, 0, 1 }, { 3, 0, 1, 2 }, };
		int lowestDistance, compareIndex, compareIndexMatch, distanceFinal, hri;
		HistResult result1, result2;

		System.out.println("Histogram result counts: " + hrlist.size() + ", " + hrlist2.size());

		for (HistResultList hr : hrlist) {
			for (HistResultList hr2 : hrlist2) {

				lowestDistance = 99999;
				compareIndex = 0;
				compareIndexMatch = 0;

				for (int[] ar : compareIndexArray) {
					compareIndex++;

					distanceFinal = 0;

					for (hri = 0; hri < hr.histResults.size(); ++hri) {
						result1 = hr.histResults.get(hri);
						distancesFirst = result1.getDistances();
						dist21 = result1.getDistances2();

						binDistance = 0;
						result2 = hr2.histResults.get(ar[hri]);

						if (result1.mDistinctiveness < result2.mMinDistinctiveness
								|| result1.mDistinctiveness > result2.mMaxDistinctiveness) {
							distanceFinal = 99999;
							break;
						}

						distancesSecond = result2.getDistances();
						dist22 = result2.getDistances2();

						for (i = 0; i < distancesFirst.length; ++i) {
							val = distancesFirst[i];
							val2 = distancesSecond[i];

							if (Math.abs(val2 - val) >= binThreshold2Negation) {
								binDistance += 2;
							}

							if (binDistance >= binThreshold) {
								break;
							}
						}

						for (i = 0; i < dist21.length && binDistance < binThreshold; ++i) {
							val = dist21[i];
							val2 = dist22[i];

							if (Math.abs(val2 - val) >= binThreshold2Negation) {
								binDistance += 2;
							}

							if (binDistance >= binThreshold) {
								break;
							}
						}

						distanceFinal += binDistance;

						if (distanceFinal >= binThreshold) {
							break;
						}
					}

					if (lowestDistance > distanceFinal) {
						compareIndexMatch = compareIndex - 1;
						lowestDistance = distanceFinal;
					}
				}

				distanceFinal = lowestDistance;

				if (distanceFinal < binThreshold) {
					FeatureMatch f = new FeatureMatch(hr.histResults.get(0).getX(), hr.histResults.get(0).getY(),
							hr2.histResults.get(0).getX(), hr2.histResults.get(0).getY());
					f.setRoughBinDistance(distanceFinal); // roughBinDistance);
					f.rotationArrayIndex = compareIndexMatch;

					featureMatches.add(f);

					g2d.setColor(Color.RED);
					// g2d.drawString(String.valueOf(hr.histResults.get(0).mDistinctiveness),
					// hr.histResults.get(0).getX() + 10, hr2.histResults.get(0).getY());
					// g2d.drawString(String.valueOf(hr2.histResults.get(0).mDistinctiveness),
					// hr2.histResults.get(0).getX() + 10 + width, hr2.histResults.get(0).getY());
				}
			}

			matchingIndex++;

			if (matchingIndex % 250 == 0) {
				System.out.println("Features compared: " + matchingIndex + "/" + hrlist.size());
			}
		}

		return featureMatches;
	}

}
