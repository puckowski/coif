package support;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {
	public static void main(String[] args) throws IOException {
		String filePath = "coifresults1.txt"; // Update this to the path of your text file
		List<ImageMatch> matches = new ArrayList<>();

		Stream<String> lines = Files.lines(Paths.get(filePath));
		List<String> lines2 = lines.collect(Collectors.toList());
		for (int index = 0; index < lines2.size(); ++index) {
			String line = lines2.get(index);
			if (line.matches(".*\\..* and .*\\..*")) {
				String firstLine = line;
				int numberOfInliers = 0;
				int totalGood = 0;
				double ratio = 0.0;

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Number of inliers:")) {
					numberOfInliers = Integer.parseInt(line.split(":")[1].trim());
				}

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Total good:")) {
					totalGood = Integer.parseInt(line.split(":")[1].trim());
				}

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Ratio of inliers/total good:")) {
					ratio = Double.parseDouble(line.split(":")[1].trim());
				}

				String[] images = firstLine.split(" and ");
				String firstImage = images[0].trim();
				String secondImage = images[1].trim();

				matches.add(new ImageMatch(firstImage, secondImage, numberOfInliers, totalGood, ratio));
			}
		}

		// Print all matches
		for (ImageMatch match : matches) {
		//	System.out.println(match);
		}

		filePath = "siftresults1.txt"; // Update this to the path of your text file
		List<ImageMatch> matches2 = new ArrayList<>();

		lines = Files.lines(Paths.get(filePath));
		lines2 = lines.collect(Collectors.toList());
		for (int index = 0; index < lines2.size(); ++index) {
			String line = lines2.get(index);
			if (line.matches(".*\\..* and .*\\..*")) {
				String firstLine = line;
				int numberOfInliers = 0;
				int totalGood = 0;
				double ratio = 0.0;

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Number of inliers:")) {
					numberOfInliers = Integer.parseInt(line.split(":")[1].trim());
				}

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Total good:")) {
					totalGood = Integer.parseInt(line.split(":")[1].trim());
				}

				index++;
				line = lines2.get(index);
				if (line != null && line.startsWith("Ratio of inliers/total good:")) {
					ratio = Double.parseDouble(line.split(":")[1].trim());
				}

				String[] images = firstLine.split(" and ");
				String firstImage = images[0].trim();
				String secondImage = images[1].trim();

				matches2.add(new ImageMatch(firstImage, secondImage, numberOfInliers, totalGood, ratio));
			}
		}

		// Print all matches
		for (ImageMatch match : matches2) {
		//	System.out.println(match);
		}
		
		ImageMatchUtils.findAndPrintMatches(matches, matches2);
	}
}