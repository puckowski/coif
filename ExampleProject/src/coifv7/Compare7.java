package coifv7;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Compare7 {
	public static int pixelCount = 0;

	public static List<HistResultList> performCircles2(int[][] image, int radius, List<MoravecResult> results,
			final int binMergeCount) {
		final List<HistResultList> resultList = new ArrayList<>();
		final int width = image.length;
		final int height = image[0].length;

		for (MoravecResult mr : results) {
			int x = mr.getX();
			int y = mr.getY();

			if (x - 4 >= 0 && y - 4 >= 0 && x + 4 < width && y + 4 < height) {
				CircleResult cr = circles3(image, mr.getX(), mr.getY() - 4, radius);
				int[] hist = cr.getHist();
				int[] hist2 = cr.getInnerHist();
				int[] center = cr.getCenterHist();

				HistResult hr = new HistResult(hist, hist2, mr.getX(), mr.getY() - 4, center);
				hr.computeDistances(binMergeCount);
				hr.computeDistinctiveness(2);

				CircleResult crSec = circles3(image, mr.getX() - 4, mr.getY(), radius);
				int[] histSec = crSec.getHist();
				int[] hist2Sec = crSec.getInnerHist();
				int[] centerSec = crSec.getCenterHist();

				HistResult hrSec = new HistResult(histSec, hist2Sec, mr.getX() - 4, mr.getY(), centerSec);
				hrSec.computeDistances(binMergeCount);
				hrSec.computeDistinctiveness(2);

				CircleResult crThird = circles3(image, mr.getX() + 4, mr.getY(), radius);
				int[] histThird = crThird.getHist();
				int[] hist2Third = crThird.getInnerHist();
				int[] centerThird = crThird.getCenterHist();

				HistResult hrThird = new HistResult(histThird, hist2Third, mr.getX() + 4, mr.getY(), centerThird);
				hrThird.computeDistances(binMergeCount);
				hrThird.computeDistinctiveness(2);

				CircleResult crFourth = circles3(image, mr.getX(), mr.getY() + 4, radius);
				int[] histFourth = crFourth.getHist();
				int[] hist2Fourth = crFourth.getInnerHist();
				int[] centerFourth = crFourth.getCenterHist();

				HistResult hrFth = new HistResult(histFourth, hist2Fourth, mr.getX(), mr.getY() + 4, centerFourth);
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

	public static void process(final String file1, final String file2, final int fileIndex)
			throws IOException {
		System.out.println("Moravec step...");

		int thresholdmor = 100;
		int ptsmor = 40;

		ImageUtils iu = new ImageUtils();
		
		BufferedImage img1 = iu.localGrayscaleArray2(file1);
		BufferedImage img2 = iu.localGrayscaleArray2(file2);

		 List<int[]> coordsA = readCoordinates("moravec3_" + fileIndex + ".txt");
		 List<int[]> coordsB = readCoordinates("v7sample" + File.separator + "moravec3_" + fileIndex + ".txt");

		 Set<String> a = new HashSet<>();
		 Set<String> b = new HashSet<>();
		 
		 for (int i = 0; i < coordsA.size(); i++) {
			 int[] co = coordsA.get(i);
			 String s = co[0] + "," + co[1] + "," + co[2] + "," + co[3];
			 a.add(s);
		 }
		 
		 for (int i = 0; i < coordsB.size(); i++) {
			 int[] co = coordsB.get(i);
			 String s = co[0] + "," + co[1] + "," + co[2] + "," + co[3];
			 b.add(s);
		 }
		 
		 List<int[]> l = new ArrayList<>();
		
		 Set<String> foo = new HashSet<>();
		 for (String bb : b) {
				 int[] list = new int[4];
				 String str = bb;
				list[0] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list[1] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list[2] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list[3] = Integer.parseInt(str);
			
				for (String ss : a) {
				int[] list2 = new int[4];
				 str = ss;
				list2[0] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list2[1] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list2[2] = Integer.parseInt(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 1);
				list2[3] = Integer.parseInt(str);
				
				  // Check for matches within ±8 range
		        boolean matchFound = true;
		       // for (int i = 0; i < list.length; i++) {
		            for (int j = 0; j < list2.length; j++) {
		                if (Math.abs(list[j] - list2[j]) > 4) {
		                    matchFound = false;
		                    break; // Break inner loop
		                }
		            }
		            if (!matchFound) break; // Break outer loop if match is found
		        //}

		        if (matchFound && !foo.contains(bb)) {
		            l.add(list);
		            foo.add(bb);
		        }
		        
				}
		 }
		 System.out.println(l.size() + " unique coordinate list size");
		 
		  // Combine images side by side
	        int width = img1.getWidth() + img2.getWidth();
	        int height = Math.max(img1.getHeight(), img2.getHeight());
	        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	        
	        Graphics2D g = combinedImage.createGraphics();
	        
	        // Draw first image on the left
	        g.drawImage(img1, 0, 0, null);
	        
	        // Draw second image on the right
	        g.drawImage(img2, img1.getWidth(), 0, null);

	        // Draw arrows between the points
	        g.setColor(Color.RED); // Set arrow color to red for visibility
	        for (int[] coord : l) {
	            int x1 = coord[0];
	            int y1 = coord[1];
	            int x2 = coord[2] + img1.getWidth(); // Adjust x2 by the width of img1
	            int y2 = coord[3];
	            drawArrowLine(g, x1, y1, x2, y2, 12, 12);
	        }
	        
	        g.dispose();

	        // Save the combined image
	        ImageIO.write(combinedImage, "png", new File(fileIndex + "_coifv7_over_coifv6.png"));
	        
		 
		System.out.println("Moravec step done.");
	}
    	
	private static List<int[]> readCoordinates(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Ignore the first line
            br.readLine(); // Ignore the second line

            // Read remaining lines into a coordinate array
            String line;
            List<int[]> list = new ArrayList<>();
            int index = 0;
            int x1= 0,y1 =0,x2 =0,y2 = 0;
            
            while ((line = br.readLine()) != null && index < 4) {
                switch( index) {
                case 0: {
                	x1 = Integer.parseInt(line);
                	break;
                }
                case 1: {
                	y1 = Integer.parseInt(line);
                	break;
                }
                case 2: {
                	x2 = Integer.parseInt(line);
                	break;
                }
                case 3: {
                	y2 = Integer.parseInt(line);
                	break;
                }
                }
                
                if (index == 3) {
                	index = 0;
                	int[] arr = { x1, y1, x2, y2 };
                	list.add(arr);
                }
                else {
                	index++;
                }
            }
            return list;
        }
    }

	
	public static double evaluateFeatureMatchCloseness(final List<FeatureMatch> featureMatches, int width, int height) {
		System.out.println("Evaluating feature match closeness...");

		int minx = Integer.MAX_VALUE, maxx = 0, miny = Integer.MAX_VALUE, maxy = 0;

		for (int i = 0; i < featureMatches.size(); ++i) {
			final FeatureMatch fm = featureMatches.get(i);

			int x = fm.getX1();
			int y = fm.getY1();

			if (minx > x) {
				minx = x;
			}

			if (miny > y) {
				miny = y;
			}

			if (maxx < x) {
				maxx = x;
			}

			if (maxy < y) {
				maxy = y;
			}
		}

		int len1 = maxx - minx;
		int len2 = maxy - miny;

		int area = len1 * len2;
		int area2 = width * height;

		double matchPercent = (double) area / (double) area2;

		System.out.println("Feature match closeness: " + matchPercent);

		return matchPercent;
	}

	public static void main(String[] args) throws IOException {
		final String[] files1 = { "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "S13_01.png", "S14_01.png", "S15_01.png", "S16_01.png",
				"S17_01.png", "S18_01.png", "S19_01.png", "S22_01.png", "S23_01.png", "S24_01.png", "S25_01.png",
				"S26_01.png", "S27_01.png", "S28_01.png", "S29_01.png", "S30_01.png", "S31_01.png", "S32_01.png",
				"S33_01.png", "S34_01.png", "S35_01.png", "S36_01.png", "biker_mural_1_candidate.jpg",
				"cars_1_candidate.jpg", "coke_190_candidate.jpg", "cup_tree_1_candidate.jpg",
				"graffiti_building_1_candidate.jpg", "graffiti_car_1_candidate.jpg",
				"no_contractor_parking_1_candidate.jpg", "P2_front_of_window_candidate.jpg", "plant_1_candidate.jpg",
				"rental_office_1_candidate.jpg", "ski_left_candidate.jpg", "stop_sign_1_candidate.jpg",
				"theory_left_candidate.jpg", "traffic_cones_1_candidate.jpg", "0_0_l.jpg", "1_1_l.jpg", "2_2_l.jpg",
				"3_3_l.jpg", "4_4_l.jpg", "5_5_l.jpg", "6_6_l.jpg", "7_7_l.jpg", "8_8_l.jpg", "9_9_l.jpg",
				"10_10_l.jpg", "11_11_l.jpg", "12_12_l.jpg", "13_13_l.jpg", "14_14_l.jpg", "15_15_l.jpg", "16_16_l.jpg",
				"17_17_l.jpg", "18_18_l.jpg", "19_19_l.jpg", "20_20_l.jpg", "21_21_l.jpg", "22_22_l.jpg", "23_23_l.jpg",
				"24_24_l.jpg", "25_25_l.jpg", "26_26_l.jpg", "27_27_l.jpg", "28_28_l.jpg", "29_29_l.jpg", "30_30_l.jpg",
				"31_31_l.jpg", "32_32_l.jpg", "33_33_l.jpg", "34_34_l.jpg", "35_35_l.jpg", "36_36_l.jpg", "37_37_l.jpg",
				"38_38_l.jpg", "39_39_l.jpg", "40_40_l.jpg", "41_41_l.jpg", "42_42_l.jpg", "Test1501_blur1.jpg",
				"Test1501_blur2.jpg", "Test1501_blur3.jpg", "Test1501_blur4.jpg", "Test1501_blur5.jpg",
				"paris_invalides_000662_sm_1.jpg", "paris_invalides_000662_sm_2.jpg", "paris_invalides_000662_sm_3.jpg",
				"paris_invalides_000662_sm_4.jpg", "paris_invalides_000662_sm_5.jpg", "paris_invalides_000662_sm_6.jpg",
				"paris_invalides_000662_sm_7.jpg", "paris_invalides_000662_sm_8.jpg", "paris_invalides_000662_sm_9.jpg",
				"paris_invalides_000662_sm_10.jpg", "paris_invalides_000662_pt_1.png",
				"paris_invalides_000662_pt_2.png", "paris_invalides_000662_pt_3.png", "paris_invalides_000662_pt_4.png",
				"paris_invalides_000662_pt_5.png", "paris_invalides_000662_pt_6.png", "paris_invalides_000662_pt_7.png",
				"paris_invalides_000662_pt_8.png", "paris_invalides_000662_pt_9.png",
				"paris_invalides_000662_pt_10.png", "paris_invalides_000662_lt_1.jpg",
				"paris_invalides_000662_lt_2.jpg", "paris_invalides_000662_lt_3.jpg", "paris_invalides_000662_lt_4.jpg",
				"paris_invalides_000662_lt_5.jpg", "paris_invalides_000662_lt_6.jpg", "paris_invalides_000662_lt_7.jpg",
				"paris_invalides_000662_lt_8.jpg", "paris_invalides_000662_lt_9.jpg", "paris_invalides_000662_1.jpg",
				"paris_invalides_000662_2.jpg", "paris_invalides_000662_3.jpg", "paris_invalides_000662_4.jpg",
				"paris_invalides_000662_5.jpg", "paris_invalides_000662_6.jpg", "paris_invalides_000662_7.jpg",
				"paris_invalides_000662_8.jpg", "paris_invalides_000662_9.jpg", "paris_invalides_000664.jpg",
				"paris_invalides_000665.jpg", "paris_invalides_000666.jpg", "paris_invalides_000667.jpg",
				"paris_invalides_000668.jpg", "paris_invalides_000669.jpg", "paris_invalides_000670.jpg",
				"paris_invalides_000671.jpg", "paris_invalides_000672.jpg", "paris_invalides_000673.jpg",
				"paris_invalides_000662.jpg", "all_souls_000065.jpg", "all_souls_000065.jpg", "pano8.jpg", "pano6.jpg",
				"1Hill.JPG", "2Hill.JPG", "S3.jpg", "b.jpg", "P1011370.JPG", "P1011069.JPG", "P1010372.JPG",
				"grail03.jpg", "DSC_0178.jpg", "bike1.png", "Yosemite1.jpg", "img2.png", "h1.jpg", "base1.jpg",
				"Test1025.jpg", "Test1027.jpg", "Test81.jpg", "Test1027.jpg", "Test1025.jpg", "Test81.jpg",
				"Test1025.jpg", "Test72.jpg", "Test65.jpg", "Test21.jpg", "Test1027.jpg", "Test1027.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test81.jpg", "Test81.jpg", "Test3000_rot.jpg", "Test47_rot.jpg",
				"Test3030.jpg", "Test1031.jpg", "Test1027.jpg", "Test1025.jpg", "Test1024.jpg", "Test506.jpg",
				"Test506.jpg", "Test404.jpg", "Test705.jpg", "Test705.jpg", "Test766.jpg", "Test766.jpg", "Test82.jpg",
				"Test5000_rot.jpg", "Test3000_rot.jpg", "Test1500.jpg", "Test1310_rot.PNG", "Test1199_rot.PNG",
				"Test1000_rot.jpg", "Test2120_rot.jpg", "Test1999_rot.jpg", "Test4.jpg", "Test6.jpg", "Test21.jpg",
				"Test34_rot.jpg", "Test37.jpg", "Test47_rot.jpg", "Test48_rot.png", "Test65.jpg", "Test70.jpg",
				"Test99.jpg", "Test120_rot.jpeg", "Test121_rot.png", "Test122_rot.png", "Test123_rot.jpg",
				"Test200_rot.jpg", "Test211_rot.jpg", "Test240.jpg", "Test300.jpg", "Test400.jpg", "Test600.jpg",
				"Test800.jpg" };
		final String[] files2 = { "Test1501_sm_1.jpg", "Test1501_sm_2.jpg", "Test1501_sm_3.jpg", "Test1501_sm_4.jpg",
				"Test1501_sm_5.jpg", "Test1501_sm_6.jpg", "Test1501_sm_7.jpg", "Test1501_sm_8.jpg", "Test1501_sm_9.jpg",
				"Test1501_sm_10.jpg", "Test1501_lg_1.jpg", "Test1501_lg_2.jpg", "Test1501_lg_3.jpg",
				"Test1501_lg_4.jpg", "Test1501_lg_5.jpg", "Test1501_lg_6.jpg", "Test1501_lg_7.jpg", "Test1501_lg_8.jpg",
				"Test1501_lg_9.jpg", "Test1501_lg_10.jpg", "S13_02.png", "S14_02.png", "S15_02.png", "S16_02.png",
				"S17_02.png", "S18_02.png", "S19_02.png", "S22_02.png", "S23_02.png", "S24_02.png", "S25_02.png",
				"S26_02.png", "S27_02.png", "S28_02.png", "S29_02.png", "S30_02.png", "S31_02.png", "S32_02.png",
				"S33_02.png", "S34_02.png", "S35_02.png", "S36_02.png", "biker_mural_1_reference.jpg",
				"cars_1_reference.jpg", "coke_190_reference.jpg", "cup_tree_1_reference.jpg",
				"graffiti_building_1_reference.jpg", "graffiti_car_1_reference.jpg",
				"no_contractor_parking_1_reference.jpg", "P2_front_of_window_reference.jpg", "plant_1_reference.jpg",
				"rental_office_1_reference.jpg", "ski_left_reference.jpg", "stop_sign_1_reference.jpg",
				"theory_left_reference.jpg", "traffic_cones_1_reference.jpg", "0_0_r.jpg", "1_1_r.jpg", "2_2_r.jpg",
				"3_3_r.jpg", "4_4_r.jpg", "5_5_r.jpg", "6_6_r.jpg", "7_7_r.jpg", "8_8_r.jpg", "9_9_r.jpg",
				"10_10_r.jpg", "11_11_r.jpg", "12_12_r.jpg", "13_13_r.jpg", "14_14_r.jpg", "15_15_r.jpg", "16_16_r.jpg",
				"17_17_r.jpg", "18_18_r.jpg", "19_19_r.jpg", "20_20_r.jpg", "21_21_r.jpg", "22_22_r.jpg", "23_23_r.jpg",
				"24_24_r.jpg", "25_25_r.jpg", "26_26_r.jpg", "27_27_r.jpg", "28_28_r.jpg", "29_29_r.jpg", "30_30_r.jpg",
				"31_31_r.jpg", "32_32_r.jpg", "33_33_r.jpg", "34_34_r.jpg", "35_35_r.jpg", "36_36_r.jpg", "37_37_r.jpg",
				"38_38_r.jpg", "39_39_r.jpg", "40_40_r.jpg", "41_41_r.jpg", "42_42_r.jpg", "Test1500.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "Test1500.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"paris_invalides_000663.jpg", "paris_invalides_000663.jpg", "paris_invalides_000663.jpg",
				"all_souls_000051.jpg", "all_souls_000066.jpg", "pano9.jpg", "pano7.jpg", "2Hill.JPG", "3Hill.JPG",
				"S5.jpg", "c.jpg", "P1011371.JPG", "P1011070.JPG", "P1010373.JPG", "grail04.jpg", "DSC_0179.jpg",
				"bike2.png", "Yosemite2.jpg", "img3.png", "h2.jpg", "base2.jpg", "Test1026_4.jpg", "Test1028_3.jpg",
				"Test85.jpg", "Test1028_2.jpg", "Test1026_3.jpg", "Test82_2.jpg", "Test1026_2.jpg", "Test70.jpg",
				"Test67.jpg", "Test23.jpg", "Test1029.jpg", "Test1030.jpg", "Test1502.jpg", "Test1503.jpg",
				"Test83.jpg", "Test84.jpg", "Test3002_rot.jpg", "Test49_rot.jpg", "Test3031.jpg", "Test1032.jpg",
				"Test1028.jpg", "Test1026.jpg", "Test1023.jpg", "Test507.jpg", "Test508.jpg", "Test405.jpg",
				"Test706.jpg", "Test707.jpg", "Test767.jpg", "Test768.jpg", "Test81.jpg", "Test5001_rot.jpg",
				"Test3001_rot.jpg", "Test1501.jpg", "Test1311_rot.PNG", "Test1200_rot.PNG", "Test1001_rot.jpg",
				"Test2121_rot.jpg", "Test2000_rot.jpg", "Test5.jpg", "Test7.jpg", "Test22.jpg", "Test35_rot.jpg",
				"Test38.jpg", "Test48_rot.jpg", "Test49_rot.png", "Test66.jpg", "Test71.jpg", "Test100.jpg",
				"Test124_rot.jpeg", "Test125_rot.png", "Test126_rot.png", "Test127_rot.jpg", "Test201_rot.jpg",
				"Test212_rot.jpg", "Test241.jpg", "Test310.jpg", "Test410.jpg", "Test610.jpg", "Test810.jpg" };

		for (int i = 0; i < files1.length; ++i) {
			System.out.println("Processing " + files1[i] + " and " + files2[i]);
			process(files1[i], files2[i], i);
		}

	}

	protected static double getMean(List<Double> values) {
		int sum = 0;
		for (double value : values) {
			sum += value;
		}

		return (sum / values.size());
	}

	public static double getVariance(List<Double> values) {
		double mean = getMean(values);
		int temp = 0;

		for (double a : values) {
			temp += (a - mean) * (a - mean);
		}

		return temp / (values.size() - 1);
	}

	public static double getStdDev(List<Double> values) {
		return Math.sqrt(getVariance(values));
	}

	public static List<Double> eliminateOutliers(List<Double> values, float scaleOfElimination) {
		final List<Double> newList = new ArrayList<>();

		if (values.size() == 1) {
			return newList;
		}

		double mean = getMean(values);
		double stdDev = getStdDev(values);

		for (double value : values) {
			boolean isLessThanLowerBound = value < mean - stdDev * scaleOfElimination;
			boolean isGreaterThanUpperBound = value > mean + stdDev * scaleOfElimination;
			boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

			if (!isOutOfBounds) {
				newList.add(value);
			}
		}

		int countOfOutliers = values.size() - newList.size();
		if (countOfOutliers == 0) {
			return values;
		}

		return eliminateOutliers(newList, scaleOfElimination);
	}

	private static void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
		int dx = x2 - x1, dy = y2 - y1;
		double D = Math.sqrt(dx * dx + dy * dy);
		double xm = D - d, xn = xm, ym = h, yn = -h, x;
		double sin = dy / D, cos = dx / D;

		x = xm * cos - ym * sin + x1;
		ym = xm * sin + ym * cos + y1;
		xm = x;

		x = xn * cos - yn * sin + x1;
		yn = xn * sin + yn * cos + y1;
		xn = x;

		int[] xpoints = { x2, (int) xm, (int) xn };
		int[] ypoints = { y2, (int) ym, (int) yn };

		g.drawLine(x1, y1, x2, y2);
		g.fillPolygon(xpoints, ypoints, 3);
	}

}
