package coifv7;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class CompareAndDraw {

	// A simple class to hold a coordinate pair (four numbers)
	public static class CoordinatePair {
		int x1, y1, x2, y2;

		public CoordinatePair(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public boolean equals(Object o) {
			CoordinatePair cp = (CoordinatePair) o;

			return Math.abs(x1 - cp.x1) < 5 && Math.abs(y1 - cp.y1) < 5 && Math.abs(x2 - cp.x2) < 5
					&& Math.abs(y2 - cp.y2) < 5;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x1, y1, x2, y2);
		}

		@Override
		public String toString() {
			return x1 + "," + y1 + "," + x2 + "," + y2;
		}
	}

	// Class to hold all data from one text file:
	// the two image filenames and a list of coordinate groups.
	public static class MatchData {
		String image1;
		String image2;
		List<CoordinatePair> coords;

		public MatchData(String image1, String image2, List<CoordinatePair> coords) {
			this.image1 = image1;
			this.image2 = image2;
			this.coords = coords;
		}
	}

	/**
	 * Parses a file with the following format: Line 1: name of image 1 (e.g.,
	 * "Test300.jpg") Line 2: name of image 2 (e.g., "Test310.jpg") Then groups of 4
	 * lines: x1, y1, x2, y2 (as integers)
	 */
	public static MatchData parseFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String img1 = br.readLine();
		String img2 = br.readLine();
		List<CoordinatePair> coords = new ArrayList<>();

		// Read all remaining numeric lines.
		List<Integer> numbers = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				try {
					numbers.add(Integer.parseInt(line));
				} catch (NumberFormatException e) {
					// If a line is not a number, ignore it.
				}
			}
		}
		br.close();

		// Group every 4 numbers into a CoordinatePair.
		for (int i = 0; i + 3 < numbers.size(); i += 4) {
			int x1 = numbers.get(i);
			int y1 = numbers.get(i + 1);
			int x2 = numbers.get(i + 2);
			int y2 = numbers.get(i + 3);
			coords.add(new CoordinatePair(x1, y1, x2, y2));
		}
		return new MatchData(img1, img2, coords);
	}

	/**
	 * Draws an arrow from (x1,y1) to (x2,y2) onto the provided Graphics2D context.
	 * It draws a line and then two short lines for the arrow head.
	 */
	public static void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		// Draw main line.
		g2d.drawLine(x1, y1, x2, y2);

		// Parameters for the arrow head.
		double phi = Math.toRadians(20); // angle of arrowhead sides
		int barb = 10; // length of arrowhead sides

		double theta = Math.atan2(y2 - y1, x2 - x1);

		// Draw arrowhead lines.
		double x, y;
		x = x2 - barb * Math.cos(theta + phi);
		y = y2 - barb * Math.sin(theta + phi);
		g2d.drawLine(x2, y2, (int) x, (int) y);

		x = x2 - barb * Math.cos(theta - phi);
		y = y2 - barb * Math.sin(theta - phi);
		g2d.drawLine(x2, y2, (int) x, (int) y);
	}

	private static BufferedImage resize(BufferedImage src, int targetSize) {
		if (targetSize <= 0) {
			return src;
		}

		int targetWidth = targetSize;
		int targetHeight = targetSize;
		float ratio = ((float) src.getHeight() / (float) src.getWidth());

		if (ratio <= 1) {
			targetHeight = (int) Math.ceil((float) targetWidth * ratio);
		} else {
			targetWidth = Math.round((float) targetHeight / ratio);
		}

		BufferedImage bi = new BufferedImage(targetWidth, targetHeight,
				src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
						: BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
		g2d.dispose();

		return bi;
	}

	public static void main(String[] args) {
		// Directories for the two sets of files.
		String fooDir = ".";
		String barDir = "v7sample";

		File barFolder = new File(barDir);
		// List files whose names start with "moravec3_" and end with ".txt"
		File[] barFiles = barFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("moravec3_") && name.endsWith(".txt");
			}
		});

		if (barFiles == null || barFiles.length == 0) {
			System.out.println("No matching files found in folder: " + barDir);
			return;
		}

		int outputIndex = 0;
		// Process each file in the bar folder.
		for (File barFile : barFiles) {
			try {
				// Parse the bar file.
				MatchData barData = parseFile(barFile);

				// Look for a corresponding file in the foo folder.
				File fooFile = new File(fooDir, barFile.getName());
				Set<CoordinatePair> fooCoords = new HashSet<>();
				if (fooFile.exists()) {
					MatchData fooData = parseFile(fooFile);
					fooCoords.addAll(fooData.coords);
				}
				// Identify coordinate groups that are in barData but not in fooCoords.
				List<CoordinatePair> diffCoords = new ArrayList<>();
				for (CoordinatePair cp : barData.coords) {
					boolean m = true;
					for (CoordinatePair c2 : fooCoords) {
						if (c2.equals(cp)) {
							m = false;
							break;
						}
					}
					if (m) {
						diffCoords.add(cp);
					}
				}

				// Load the two images.
				BufferedImage img1 = ImageIO.read(new File(barData.image1));
				img1 = resize(img1, 640);
				BufferedImage img2 = ImageIO.read(new File(barData.image2));
				img2 = resize(img2, 640);
				if (img1 == null || img2 == null) {
					System.err.println("Could not load one or both images for file: " + barFile.getName());
					continue;
				}

				// Create a new BufferedImage to hold both images side-by-side.
				int combinedWidth = img1.getWidth() + img2.getWidth();
				int combinedHeight = Math.max(img1.getHeight(), img2.getHeight());
				BufferedImage combined = new BufferedImage(combinedWidth, combinedHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = combined.createGraphics();

				// Draw the images:
				// img1 goes at (0,0) and img2 goes at (img1.getWidth(), 0)
				g2d.drawImage(img1, 0, 0, null);
				g2d.drawImage(img2, img1.getWidth(), 0, null);

				// Set up arrow drawing style.
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(2));

				// Draw an arrow for each coordinate difference.
				// Note: (x1, y1) is in the left image (img1) and (x2, y2) is in the right image
				// (img2)
				// so we offset x2 by img1.getWidth().
				for (CoordinatePair cp : diffCoords) {
					int startX = cp.x1;
					int startY = cp.y1;
					int endX = cp.x2 + img1.getWidth(); // shift right image’s coordinates
					int endY = cp.y2;
					drawArrow(g2d, startX, startY, endX, endY);
				}

				g2d.dispose();

				// Save the resulting image with an index in the filename.
				String outputFileName = "result_" + outputIndex + ".png";
				ImageIO.write(combined, "PNG", new File(outputFileName));
				System.out.println("Saved " + outputFileName);
				outputIndex++;

			} catch (IOException e) {
				System.err.println("Error processing file: " + barFile.getName());
				e.printStackTrace();
			}
		}
	}
}