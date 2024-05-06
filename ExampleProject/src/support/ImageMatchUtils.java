package support;
import java.util.List;

public class ImageMatchUtils {

    public static void findAndPrintMatches(List<ImageMatch> coifMatches, List<ImageMatch> siftMatches) {
        int count = 0;
        int coif = 0;
    	for (ImageMatch coifItem : coifMatches) {
            for (ImageMatch siftItem : siftMatches) {
                if (coifItem.firstImage.equals(siftItem.firstImage) && coifItem.secondImage.equals(siftItem.secondImage)) {
                    System.out.println("Match found: ");
                    System.out.println("List1 - " + coifItem);
                    System.out.println("List2 - " + siftItem);
                    System.out.println("COIF to SIFT: " + coifItem.ratio + " vs. " + siftItem.ratio);
                    if (coifItem.ratio < siftItem.ratio) {
                    	System.out.println("SIFT better");
                    } else if (coifItem.ratio == siftItem.ratio ){
                    	System.out.println("COIF and SIFT same");
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