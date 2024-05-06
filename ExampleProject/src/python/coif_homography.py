import cv2
import matplotlib.pyplot as plt
import numpy as np
import random
from tqdm import tqdm_notebook as tqdm
plt.rcParams['figure.figsize'] = [15, 15]
import glob

DROPOUT_RATE = 0

files1 = glob.glob('moravec3_*.txt')
cnt = 0

ptsdisk1 = []
ptsdisk2 = []

file1 = ""
file2 = ""

for file in files1: 
	print("img file " + file)
	filepath = file

	# Using readlines()
	file1 = open(filepath, 'r')
	Lines = file1.readlines()
  
	count = 0
	arr1 = []
	arr2 = []
	arr = []
	arrdest = []
	for line in Lines:
		line = line.strip()

		if count == 0:
			file1 = line
		elif count == 1:
			file2 = line 
		elif count > 1:
			if len(arr1) < 2:
				arr1.append(int(line))
			elif len(arr2) < 2:
				arr2.append(int(line))
			
			if len(arr2) == 2:
				random_number = random.randint(0, DROPOUT_RATE)

				if (random_number == 0):
					arr.append(arr1)
					arrdest.append(arr2)
				arr1 = []
				arr2 = []
		count = count + 1	
	ptsdisk1.clear()
	ptsdisk2.clear()
	
	ptsdisk1.append(arr)
	ptsdisk2.append(arrdest)   

	# Use your own src_pts and dst_pts
	src_pts = np.float32(ptsdisk1[0])
	dst_pts = np.float32(ptsdisk2[0])
	print(str(len(src_pts)) + " and " + str(len(dst_pts)))

	H, mask = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC,100.0)

	# Number of inliers
	inliers = np.sum(mask)
	print(file1 + " and " + file2)
	print("Number of inliers: ", inliers)
	print("Total good: " + str(len(src_pts)))
		
	total_good = len(src_pts)

	# Calculate the ratio
	if total_good > 0:  # Prevent division by zero
		ratio = inliers / total_good
	else:
		ratio = 0  # Handle case where total_good is zero

	print(f"Ratio of inliers/total good: {ratio:.4f}")  # Format ratio to 2 decimal places
