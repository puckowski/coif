import cv2
import numpy as np
import glob

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
                print("1st arr ")
                print(line)
                arr1.append(int(line))
            elif len(arr2) < 2:
                print("2nd arr ")
                print(line)
                arr2.append(int(line))
            
            if len(arr2) == 2:
                arr.append(arr1)
                arrdest.append(arr2)
                arr1 = []
                arr2 = []
        count = count + 1
    ptsdisk1.append(arr)
    ptsdisk2.append(arrdest)   

# Read the images
image1 = cv2.imread(file1)
image2 = cv2.imread(file2)

# Compute the maximum width and height of the input images
max_width = max(image1.shape[1], image2.shape[1])
max_height = max(image1.shape[0], image2.shape[0])

# Use your own src_pts and dst_pts
src_pts = np.float32(ptsdisk1[0])
dst_pts = np.float32(ptsdisk2[0])

M, mask = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC,100.0)

h,w = image1.shape[:2]
pts = np.float32([ [0,0],[0,h-1],[w-1,h-1],[w-1,0] ]).reshape(-1,1,2)
print(pts)
print("and M:")
print(M)

dst = cv2.perspectiveTransform(pts, M)
image2 = cv2.polylines(image2,[np.int32(dst)],True,255,3, cv2.LINE_AA)


# Use the homography matrix to warp the first image and combine it with the second image
result = cv2.warpPerspective(image1, M,  (max_width, max_height))
result[0:image2.shape[0], 0:image2.shape[1]] = image2

# Save the result
cv2.imwrite("result.jpg", result)
