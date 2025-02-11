package coifv7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimeData {

	public static int imageLoad = 0;
	public static int moravec = 0;
	public static int matching = 0;

	public static List<Long> matchingTimes = new ArrayList<>();
	public static List<Long> moravecTimes = new ArrayList<>();
	public static List<Long> distinctivenessAlignmentTimes = new ArrayList<>();

	public static int[] binDistanceUsage = new int[6];
	public static int[] iterationCountUsage = new int[10];
	public static HashMap<Integer, List<Integer>> iterationCountUsageMap = new HashMap<>();
	
	public static final List<String> quickFiles = new ArrayList<>();
	
	public static final Map<Integer, Integer> rotationMap = new HashMap<>();
	
	public static int featureMatches = 0;

	static {
		for (int i = 0; i < binDistanceUsage.length; ++i) {
			binDistanceUsage[i] = 0;
		}
	}
}
