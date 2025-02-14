# Concentric Oval Intensity Features (COIF)

Daniel Puckowski

## Abstract

In this paper, I present a novel quasi-rotation invariant interest point descriptor, coined COIF (Concentric Oval Intensity Features). The descriptor is straightforward to implement and feature matching is time efficient. COIF may be used to detect rotated images and may be used for image stitching in panorama applications. COIF demonstrates the feasibility of using luminance histograms for feature matching.

![Example COIFv6 Result](https://raw.githubusercontent.com/puckowski/coif/master/Result_COIFv6.png)

## General Comparison

| Description       | SIFT   | COIF   |
|-------------------|--------|--------|
| Instances Equal   | 55     | 55     |
| SIFT Better       | 11     | -      |
| COIF Better       | -      | 8      |
| Accuracy (%)      | 98.9589| 98.5205|
| More Accurate (%) | +0.4384| -      |

## Detailed Accuracy Distribution

### COIFv6

| Accuracy Range | Count |
|----------------|-------|
| 100%           | 60    |
| 99-95%         | 6     |
| 94-90%         | 4     |
| 89-85%         | 0     |
| 84-80%         | 3     |

### SIFT

| Accuracy Range | Count |
|----------------|-------|
| 100%           | 65    |
| 99-95%         | 4     |
| 94-90%         | 1     |
| 89-85%         | 2     |
| 84-80%         | 0     |
| 79-75%         | 1     |

## Image Stitching Dataset Performance

| Dataset                                                  | COIFv6 Success | COIFv6 Failure | SIFT Success | SIFT Failure | COIFv6 vs. SIFT |
|----------------------------------------------------------|----------------|----------------|--------------|--------------|-----------------|
| SPW Dataset (2020)                                       | 88.373%        | 11.627%        | 95.455%      | 4.545%       | -7.082%         |
| Dataset for Stitching with Multiple Registrations (2018) | 65.286%        | 35.714%        | 50.000%      | 50.000%      | +15.286%        |
| VPG Dataset (2020)                                       | 90.910%        | 9.090%         | 44.455%      | 54.545%      | +46.455%        |

## Impact of Environmental Factors on Measurement Accuracy

| Effect                     | Accuracy Range  |
|----------------------------|-----------------|
| Light Variation            | +/- 10%         |
| Perspective Transformation | 25%             |
| Scale Change               | +/- 20%         |
| Guassian Blur              | +3 pixel radius |

## Performance Metrics and Distribution Statistics for Image Matching Operations

| Average Matching Time | Median Matching Time | Image Pair Count | Pixels Processed Count |
|-----------------------|----------------------|------------------|------------------------|
| 4,283 milliseconds    | 1,985 milliseconds   | 45               | 25,543,680             |

## Performance Metrics and Distribution Statistics for COIFv6 Upright (Minimal Image Rotation)

| Average Matching Time | Median Matching Time | Image Pair Count | Pixels Processed Count |
|-----------------------|----------------------|------------------|------------------------|
| 3,778 milliseconds    | 1,577 milliseconds   | 45               | 25,543,680             |

Matching times include time to identify corners, time to generate descriptors, and time for feature matching.

| Bin Merge Count | Number of Times Used | Percent Occurrence |
|-----------------|----------------------|--------------------|
| 1               | 38                   | 69.09%             |
| 2               | 3                    | 5.45%              |
| 3               | 4                    | 7.27%              |
| 4               | 5                    | 9.09%              |
| 5               | 5                    | 9.09%              |

## Detailed Analysis of Iteration Counts by Bin Merge

| Bin Merge Count | Iteration | Count | Percent Occurrence |
|-----------------|-----------|-------|--------------------|
| 1               | 1         | 66    | 51.96%             |
| 2               | 1         | 16    | 12.59%             |
| 3               | 1         | 11    | 8.66%              |
| 4               | 1         | 11    | 8.66%              |
| 4               | 2         | 1     | 0.78%              |
| 4               | 6         | 1     | 0.78%              |
| 5               | 1         | 7     | 5.51%              |
| 5               | 2         | 2     | 1.57%              |
| 5               | 4         | 1     | 0.78%              |
| 5               | 5         | 1     | 0.78%              |
| 5               | 7         | 2     | 1.57%              |
| 5               | 8         | 1     | 0.78%              |
| 5               | 9         | 7     | 5.51%              |

Given the test image pair set, 51.96% of all image pairs yielded passing feature matches with the default COIFv6 parameters.
Given the test image pair set, 87.38% of all image pairs yielded passing feature matches within the first 5 iterations.
