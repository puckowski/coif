package support;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ImageMatch {
    String firstImage;
    String secondImage;
    int numberOfInliers;
    int totalGood;
    double ratio;

    public ImageMatch(String firstImage, String secondImage, int numberOfInliers, int totalGood, double ratio) {
        this.firstImage = firstImage;
        this.secondImage = secondImage;
        this.numberOfInliers = numberOfInliers;
        this.totalGood = totalGood;
        this.ratio = ratio;
    }

    @Override
    public String toString() {
        return "ImageMatch{" +
                "firstImage='" + firstImage + '\'' +
                ", secondImage='" + secondImage + '\'' +
                ", numberOfInliers=" + numberOfInliers +
                ", totalGood=" + totalGood +
                ", ratio=" + ratio +
                '}';
    }
}
