
import cv2 
import matplotlib.pyplot as plt
import time
import statistics
import math

sift = cv2.SIFT_create()

files1 = [
"biker_mural_1_reference.jpg",
"cars_1_reference.jpg",
"coke_190_reference.jpg",
"cup_tree_1_reference.jpg",
"graffiti_building_1_reference.jpg",
"graffiti_car_1_reference.jpg",
"no_contractor_parking_1_reference.jpg",
"P2_front_of_window_reference.jpg",
"plant_1_reference.jpg",
"rental_office_1_reference.jpg",
"ski_left_reference.jpg",
"stop_sign_1_reference.jpg",
"theory_left_reference.jpg",
"traffic_cones_1_reference.jpg", ]
files2 = [ 
"biker_mural_1_candidate.jpg",
"cars_1_candidate.jpg",
"coke_190_candidate.jpg",
"cup_tree_1_candidate.jpg",
"graffiti_building_1_candidate.jpg",
"graffiti_car_1_candidate.jpg",
"no_contractor_parking_1_candidate.jpg",
"P2_front_of_window_candidate.jpg",
"plant_1_candidate.jpg",
"rental_office_1_candidate.jpg",
"ski_left_candidate.jpg",
"stop_sign_1_candidate.jpg",
"theory_left_candidate.jpg",
"traffic_cones_1_candidate.jpg"]

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