package coifv7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistResultFilter {

    /**
     * Filters a list of HistResult objects by removing those that are part of a cluster
     * that is too “dense” (i.e. 10 or more objects in the cluster).
     *
     * Two HistResult objects are considered in the same cluster if:
     *   - Their x and y coordinates differ by at most pixelRange (e.g. 50 pixels)
     *   - Their sparse region (sr) lists are “similar.” Two sr lists are similar if they
     *     have the same number of Region objects and for each corresponding Region,
     *     the difference in length is within regionTolerance.
     *
     * @param histResults      the list of HistResult objects to filter.
     * @param pixelRange       the maximum allowed difference in x and y coordinates (e.g. 50).
     * @param clusterThreshold the minimum cluster size (e.g. 10) for removal.
     * @param regionTolerance  the maximum allowed difference in region lengths.
     * @return a filtered list of HistResult objects.
     */
	public static Map<HistResultList, HistResult> filterHistResults(
	        Map<HistResultList, HistResult> histResults,
	        int pixelRange,
	        int clusterThreshold) {

	    // Convert the map entries to a list to allow index-based iteration.
	    List<Map.Entry<HistResultList, HistResult>> entries = new ArrayList<>(histResults.entrySet());

	    // Set to keep track of HistResult objects to remove.
	    Set<HistResult> toRemove = new HashSet<>();
	    // Boolean array to mark which entries have been processed.
	    boolean[] processed = new boolean[entries.size()];

	    // Loop over the list of entries.
	    for (int i = 0; i < entries.size(); i++) {
	        if (processed[i]) {
	            continue;
	        }
	        HistResult current = entries.get(i).getValue();
	        // Start a new cluster with the current object.
	        List<Integer> clusterIndices = new ArrayList<>();
	        clusterIndices.add(i);
	        processed[i] = true;

	        // Check subsequent entries to see if they belong in the same cluster.
	        for (int j = i + 1; j < entries.size(); j++) {
	            if (processed[j]) {
	                continue;
	            }
	            HistResult candidate = entries.get(j).getValue();
	            if (isWithinRange(current, candidate, pixelRange)
	                    && (Math.abs(current.mLongestSequence - candidate.mLongestSequence) < 3
	                        && current.mLongestSequence >= 70)) {
	                clusterIndices.add(j);
	                processed[j] = true;
	            }
	        }

	        // If the cluster size meets or exceeds the threshold, mark all its members for removal.
	        if (clusterIndices.size() >= clusterThreshold) {
	            for (int index : clusterIndices) {
	                toRemove.add(entries.get(index).getValue());
	            }
	        }
	    }

	    // Build a new map with only the HistResults not marked for removal.
	    Map<HistResultList, HistResult> filtered = new HashMap<>();
	    for (Map.Entry<HistResultList, HistResult> entry : histResults.entrySet()) {
	        if (!toRemove.contains(entry.getValue())) {
	            filtered.put(entry.getKey(), entry.getValue());
	        }
	    }
	    return filtered;
	}

    /**
     * Checks if two HistResult objects are spatially near each other.
     * They are considered near if their x and y differences are both <= range.
     *
     * @param a     the first HistResult.
     * @param b     the second HistResult.
     * @param range the maximum allowed difference.
     * @return true if they are within range; false otherwise.
     */
    private static boolean isWithinRange(HistResult a, HistResult b, int range) {
        return Math.abs(a.getX() - b.getX()) <= range &&
               Math.abs(a.getY() - b.getY()) <= range;
    }

    /**
     * Checks if two lists of Region objects (sr) are similar.
     * Two sr lists are considered similar if they have the same number of regions,
     * and each corresponding region’s length (end - start) differs by no more than tolerance.
     *
     * @param regions1      the first list of Region objects.
     * @param regions2      the second list of Region objects.
     * @param tolerance     the maximum allowed difference in region lengths.
     * @return true if the lists are similar; false otherwise.
     */

}