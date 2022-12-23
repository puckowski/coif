import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUtils {

	public ImageUtils() {

	}

	public BufferedImage markupGrayscaleWithResults(int[][] grayValues, List<MoravecResult> results) {
		if (grayValues.length == 0) {
			return null;
		}

		BufferedImage newImage = new BufferedImage(grayValues.length, grayValues[0].length, BufferedImage.TYPE_INT_RGB);

		int x;
		int y;
		int red;
		int green;
		int blue;
		Color currColor;

		for (x = 0; x < grayValues.length; ++x) {
			for (y = 0; y < grayValues[x].length; ++y) {
				red = grayValues[x][y];
				green = grayValues[x][y];
				blue = grayValues[x][y];

				currColor = new Color(red, green, blue);
				newImage.setRGB(x, y, currColor.getRGB());
			}
		}

		Graphics2D g2d = newImage.createGraphics();

		for (MoravecResult result : results) {
			g2d.setColor(Color.RED);
			g2d.fillOval(result.getX() - 3, result.getY() - 3, 6, 6);
		}

		return newImage;
	}

	public BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	public int[][] localGrayscaleArray(final String absoluteFileName) throws IOException {
		BufferedImage image = ImageIO.read(new File(absoluteFileName));

		int[][] redValues = new int[image.getWidth()][image.getHeight()];
		int[][] greenValues = new int[image.getWidth()][image.getHeight()];
		int[][] blueValues = new int[image.getWidth()][image.getHeight()];

		Color currColor;
		int x;
		int y;

		for (x = 0; x < image.getWidth(); ++x) {
			for (y = 0; y < image.getHeight(); ++y) {
				currColor = new Color(image.getRGB(x, y));

				redValues[x][y] = currColor.getRed();
				greenValues[x][y] = currColor.getGreen();
				blueValues[x][y] = currColor.getBlue();
			}
		}

		int[][] grayValues = new int[image.getWidth()][image.getHeight()];

		int red;
		int green;
		int blue;
		int gray;

		for (x = 0; x < image.getWidth(); ++x) {
			for (y = 0; y < image.getHeight(); ++y) {
				red = (int) Math.round(redValues[x][y] * 0.299);
				green = (int) Math.round(greenValues[x][y] * 0.114);
				blue = (int) Math.round(blueValues[x][y] * 0.587);

				gray = red + green + blue;

				grayValues[x][y] = gray;
			}
		}

		return grayValues;
	}
}
