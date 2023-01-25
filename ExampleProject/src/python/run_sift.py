import cv2 
import matplotlib.pyplot as plt
import time
import statistics
import math

sift = cv2.xfeatures2d.SIFT_create(nfeatures=5000)

files1 = [ "Test5000.jpg", "Test3000.jpg", "Test3000.jpg", "Test1500.jpg", "Test1310.PNG", "Test1199.PNG", "Test1000.jpg", "Test2120.jpg", "Test1999.jpg", "Test4.jpg", "Test6.jpg", "Test21.jpg", "Test34.jpg", "Test37.jpg", "Test47.jpg", "Test48.png", "Test65.jpg", "Test70.jpg", "Test99.jpg","Test120.jpeg", "Test121.png", "Test122.png", "Test123.jpg", "Test200.jpg", "Test211.jpg", "Test240.jpg", "Test300.jpg", "Test400.jpg", "Test600.jpg", "Test800.jpg" ]
files2 = [ "Test5001.jpg", "Test3002.jpg", "Test3001.jpg", "Test1501.jpg", "Test1311.PNG", "Test1200.PNG", "Test1001.jpg", "Test2121.jpg", "Test2000.jpg", "Test5.jpg", "Test7.jpg", "Test22.jpg", "Test35.jpg", "Test38.jpg", "Test48.jpg", "Test49.png", "Test66.jpg", "Test71.jpg", "Test100.jpg", "Test124.jpeg", "Test125.png", "Test126.png", "Test127.jpg", "Test201.jpg", "Test212.jpg","Test241.jpg", "Test310.jpg", "Test410.jpg", "Test610.jpg", "Test810.jpg" ]
        
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