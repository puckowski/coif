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
