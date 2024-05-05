package support;
import java.util.List;

public class ImageMatchUtils {

    public static void findAndPrintMatches(List<ImageMatch> list1, List<ImageMatch> list2) {
        int count = 0;
        int coif = 0;
    	for (ImageMatch item1 : list1) {
            for (ImageMatch item2 : list2) {
                if (item1.firstImage.equals(item2.firstImage) && item1.secondImage.equals(item2.secondImage)) {
                    System.out.println("Match found: ");
                    System.out.println("List1 - " + item1);
                    System.out.println("List2 - " + item2);
                    System.out.println("COIF to SIFT: " + item1.ratio + " vs. " + item2.ratio);
                    if (item1.ratio < item2.ratio) {
                    	System.out.println("SIFT better");
                    } else if (item1.ratio == item2.ratio ){
                    	System.out.println("SIFT and COIF same");
                    	coif++;
                    } else {
                    	System.out.println("COIF better");
                    	coif++;
                    }
                    System.out.println();
                    count++;
                }
            }
        }
    	System.out.println("COIF was better or equal " + coif + " out of " + count + " image pairs");
    }
}