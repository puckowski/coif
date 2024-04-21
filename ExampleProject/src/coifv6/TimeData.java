package coifv6;

import java.util.ArrayList;
import java.util.List;

public class TimeData {

	public static int imageLoad = 0;
	public static int moravec = 0;
	public static int matching = 0;

	public static List<Long> matchingTimes = new ArrayList<>();
	public static List<Long> moravecTimes = new ArrayList<>();

	public static int[] binDistanceUsage = new int[6];
	
	public static int featureMatches = 0;

	static {
		for (int i = 0; i < binDistanceUsage.length; ++i) {
			binDistanceUsage[i] = 0;
		}
	}
}
