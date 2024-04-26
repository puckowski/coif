package coifv6;

import java.util.ArrayList;
import java.util.List;

public class HistResultList {

	public List<HistResult> histResults;

	public HistResultList() {
		histResults = new ArrayList<>();
	}

	public boolean distinctivenessLessThan(int threshold) {
		double sum = 0.0;
		for (HistResult h : histResults) {
			/*if (h.mDistinctiveness < threshold) {
				return true;
			}*/
			sum += h.mDistinctiveness;
		}
		sum /= histResults.size();
		
		if (sum < threshold) {
			return true;
		}

		return false;
	}

	public boolean distinctivenessGreaterThan(int threshold) {
		for (HistResult h : histResults) {
			if (h.mDistinctiveness > threshold) {
				return true;
			}
		}

		return false;
	}
}
