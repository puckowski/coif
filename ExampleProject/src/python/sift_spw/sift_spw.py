
import cv2 
import matplotlib.pyplot as plt
import time
import statistics
import math

sift = cv2.SIFT_create()

files1 = ["0_0_l.jpg",
"1_1_l.jpg",
"2_2_l.jpg",
"3_3_l.jpg",
"4_4_l.jpg",
"5_5_l.jpg",
"6_6_l.jpg",
"7_7_l.jpg",
"8_8_l.jpg",
"9_9_l.jpg",
"10_10_l.jpg",
"11_11_l.jpg",
"12_12_l.jpg",
"13_13_l.jpg",
"14_14_l.jpg",
"15_15_l.jpg",
"16_16_l.jpg",
"17_17_l.jpg",
"18_18_l.jpg",
"19_19_l.jpg",
"20_20_l.jpg",
"21_21_l.jpg",
"22_22_l.jpg",
"23_23_l.jpg",
"24_24_l.jpg",
"25_25_l.jpg",
"26_26_l.jpg",
"27_27_l.jpg",
"28_28_l.jpg",
"29_29_l.jpg",
"30_30_l.jpg",
"31_31_l.jpg",
"32_32_l.jpg",
"33_33_l.jpg",
"34_34_l.jpg",
"35_35_l.jpg",
"36_36_l.jpg",
"37_37_l.jpg",
"38_38_l.jpg",
"39_39_l.jpg",
"40_40_l.jpg",
"41_41_l.jpg",
"42_42_l.jpg",

]
files2 = ["0_0_r.jpg",
"1_1_r.jpg",
"2_2_r.jpg",
"3_3_r.jpg",
"4_4_r.jpg",
"5_5_r.jpg",
"6_6_r.jpg",
"7_7_r.jpg",
"8_8_r.jpg",
"9_9_r.jpg",
"10_10_r.jpg",
"11_11_r.jpg",
"12_12_r.jpg",
"13_13_r.jpg",
"14_14_r.jpg",
"15_15_r.jpg",
"16_16_r.jpg",
"17_17_r.jpg",
"18_18_r.jpg",
"19_19_r.jpg",
"20_20_r.jpg",
"21_21_r.jpg",
"22_22_r.jpg",
"23_23_r.jpg",
"24_24_r.jpg",
"25_25_r.jpg",
"26_26_r.jpg",
"27_27_r.jpg",
"28_28_r.jpg",
"29_29_r.jpg",
"30_30_r.jpg",
"31_31_r.jpg",
"32_32_r.jpg",
"33_33_r.jpg",
"34_34_r.jpg",
"35_35_r.jpg",
"36_36_r.jpg",
"37_37_r.jpg",
"38_38_r.jpg",
"39_39_r.jpg",
"40_40_r.jpg",
"41_41_r.jpg",
"42_42_r.jpg",]

i = 0
timeTotal = 0
allTimes = []

while i < len(files1):
    print(files1[i] + " and " + files2[i])
    
    # read images
    img1 = cv2.imread(files1[i])  
    img2 = cv2.imread(files2[i]) 

    img1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
    img2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)

    start = time.time()

    #sift
    print("detect...")
    
    keypoints_1, descriptors_1 = sift.detectAndCompute(img1,None)
    keypoints_2, descriptors_2 = sift.detectAndCompute(img2,None)

    print("matching...")
    
    #feature matching
    bf = cv2.BFMatcher(cv2.NORM_L1, crossCheck=True)

    matches = bf.match(descriptors_1,descriptors_2)

    end = time.time()
    print(end - start)
    
    timeTotal += end - start
    allTimes.append(end - start)
    
    matches = sorted(matches, key = lambda x:x.distance)

    img3 = cv2.drawMatches(img1, keypoints_1, img2, keypoints_2, matches[:50], img2, flags=2)
    cv2.imwrite("moravec_" + str(i) + ".jpg", img3)
    
    i += 1        
    
print("All files processed...")
print("Total time in seconds:")
print(timeTotal)

avg = 0.0
for time in allTimes:
    avg += time
avg /= len(files1)

print("Average time in seconds:")
print(avg)

print("Median time in seconds:")
print(statistics.median(map(float, allTimes)))

def getMean(values): 
	sum = 0
	for value in values:
		sum += value

	return (sum / len(values))

def getVariance(values):
	mean = getMean(values)
	temp = 0

	for a in values: 
		temp += (a - mean) * (a - mean)

	return temp / (len(values) - 1)

def getStdDev(values): 
	return math.sqrt(getVariance(values))

def eliminateOutliers(values, scaleOfElimination):
    mean = getMean(values)
    stdDev = getStdDev(values)

    newList = []

    for value in values:
        isLessThanLowerBound = value < mean - stdDev * scaleOfElimination
        isGreaterThanUpperBound = value > mean + stdDev * scaleOfElimination
        isOutOfBounds = isLessThanLowerBound or isGreaterThanUpperBound

        if isOutOfBounds == False: 
            newList.append(value)

    countOfOutliers = len(values) - len(newList)
		
    if countOfOutliers == 0:
        return values

    return eliminateOutliers(newList, scaleOfElimination)


outliers = eliminateOutliers(allTimes, 2)
remaining = [];
remSum = 0.0
for time in allTimes:
    if time not in outliers:
        remaining.append(time)
        remSum += time

print("Average without outliers in seconds: ")
print(str(remSum / len(files1)) + " average without outliers")