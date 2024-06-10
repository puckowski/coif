
import cv2 
import matplotlib.pyplot as plt
import time
import statistics
import math

sift = cv2.SIFT_create()

files1 = [ "S13_01.png",
"S14_01.png",
"S15_01.png",
"S16_01.png",
"S17_01.png",
"S18_01.png",
"S19_01.png",
"S22_01.png",
"S23_01.png",
"S24_01.png",
"S25_01.png",
"S26_01.png",
"S27_01.png",
"S28_01.png",
"S29_01.png",
"S30_01.png",
"S31_01.png",
"S32_01.png",
"S33_01.png",
"S34_01.png",
"S35_01.png",
"S36_01.png" ]
files2 = [ 
"S13_02.png",
"S14_02.png",
"S15_02.png",
"S16_02.png",
"S17_02.png",
"S18_02.png",
"S19_02.png",
"S22_02.png",
"S23_02.png",
"S24_02.png",
"S25_02.png",
"S26_02.png",
"S27_02.png",
"S28_02.png",
"S29_02.png",
"S30_02.png",
"S31_02.png",
"S32_02.png",
"S33_02.png",
"S34_02.png",
"S35_02.png",
"S36_02.png",]

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