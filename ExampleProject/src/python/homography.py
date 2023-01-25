import cv2
import matplotlib.pyplot as plt
import numpy as np
import random
from tqdm import tqdm_notebook as tqdm
plt.rcParams['figure.figsize'] = [15, 15]
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

height, width, channels = image1.shape

down_width1 = 0
down_height1 = 0

if height >= width:
    down_height1 = 640
    pct = down_height1 / height
    down_width1 = pct * width

if width > height:
    down_width1 = 640
    pct = down_width1 / width
    down_height1 = pct * height

down_points = (int(down_width1), int(down_height1))
resized_down1 = cv2.resize(image1, down_points, interpolation= cv2.INTER_LINEAR)

height, width, channels = image2.shape

down_width1 = 0
down_height1 = 0

if height >= width:
    down_height1 = 640
    pct = down_height1 / height
    down_width1 = pct * width

if width > height:
    down_width1 = 640
    pct = down_width1 / width
    down_height1 = pct * height

down_points2 = (int(down_width1), int(down_height1))
resized_down2 = cv2.resize(image2, down_points2, interpolation= cv2.INTER_LINEAR)

img_rgb1 = cv2.cvtColor(resized_down1, cv2.COLOR_BGR2RGB)
img_rgb2 = cv2.cvtColor(resized_down2, cv2.COLOR_BGR2RGB)

height3, width3, channels3 = img_rgb1.shape
height4, width4, channels4 = img_rgb2.shape
print(str(height3) + " " + str(width3))

# Use your own src_pts and dst_pts
src_pts = np.float32(ptsdisk1[0])
dst_pts = np.float32(ptsdisk2[0])

for point in src_pts:
    cv2.circle(img_rgb1, (int(point[0]), int(point[1])), 5, (0, 0, 255), -1)
for point in dst_pts:
    cv2.circle(img_rgb2, (int(point[0]), int(point[1])), 5, (0, 0, 255), -1)

cv2.imshow("Image1", img_rgb1)
cv2.imshow("Image2", img_rgb2)

H, mask = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC,100.0)

def plot_matches2(total_img):
    match_img = total_img.copy()
    fig, ax = plt.subplots()
    ax.set_aspect('equal')
    ax.imshow(np.array(match_img).astype('uint8')) #　RGB is integer type

    plt.show()

def get_error(points, H):
    num_points = len(points)
    all_p1 = np.concatenate((points[:, 0:2], np.ones((num_points, 1))), axis=1)
    all_p2 = points[:, 2:4]
    estimate_p2 = np.zeros((num_points, 2))
    for i in range(num_points):
        temp = np.dot(H, all_p1[i])
        estimate_p2[i] = (temp/temp[2])[0:2] # set index 2 to 1 and slice the index 0, 1
    # Compute error
    errors = np.linalg.norm(all_p2 - estimate_p2 , axis=1) ** 2

    return errors

def stitch_img(left, right, H):
    print("stiching image ...")
    
    # Convert to double and normalize. Avoid noise.
    left = cv2.normalize(left.astype('float'), None, 
                            0.0, 1.0, cv2.NORM_MINMAX)   
    # Convert to double and normalize.
    right = cv2.normalize(right.astype('float'), None, 
                            0.0, 1.0, cv2.NORM_MINMAX)   
    
    # left image
    height_l, width_l, channel_l = left.shape
    corners = [[0, 0, 1], [width_l, 0, 1], [width_l, height_l, 1], [0, height_l, 1]]
    corners_new = [np.dot(H, corner) for corner in corners]
    corners_new = np.array(corners_new).T 
    x_news = corners_new[0] / corners_new[2]
    y_news = corners_new[1] / corners_new[2]
    y_min = min(y_news)
    x_min = min(x_news)

    translation_mat = np.array([[1, 0, -x_min], [0, 1, -y_min], [0, 0, 1]])
    H = np.dot(translation_mat, H)
    
    # Get height, width
    height_new = int(round(abs(y_min) + height_l))
    width_new = int(round(abs(x_min) + width_l))
    size = (width_new, height_new)

    # right image
    warped_l = cv2.warpPerspective(src=left, M=H, dsize=size)

    height_r, width_r, channel_r = right.shape
    
    height_new = int(round(abs(y_min) + height_r))
    width_new = int(round(abs(x_min) + width_r))
    size = (width_new, height_new)
    

    warped_r = cv2.warpPerspective(src=right, M=translation_mat, dsize=size)
     
    black = np.zeros(3)  # Black pixel.
    
    # Stitching procedure, store results in warped_l.
    for i in range(warped_r.shape[0]):
        print(str(i) + '/' + str(warped_r.shape[0]))
        for j in range(warped_r.shape[1]):
            pixel_l = warped_l[i, j, :]
            pixel_r = warped_r[i, j, :]
            
            if not np.array_equal(pixel_l, black) and np.array_equal(pixel_r, black):
                warped_l[i, j, :] = pixel_l
            elif np.array_equal(pixel_l, black) and not np.array_equal(pixel_r, black):
                warped_l[i, j, :] = pixel_r
            elif not np.array_equal(pixel_l, black) and not np.array_equal(pixel_r, black):
                warped_l[i, j, :] = pixel_l # (pixel_l + pixel_r) / 2
            else:
                pass
                  
    stitch_image = warped_l[:warped_r.shape[0], :warped_r.shape[1], :]
    return stitch_image

print(H)
cv2.waitKey(0)
result1 = stitch_img(img_rgb1, img_rgb2, H)
plt.imshow(result1)
plot_matches2(result1)
cv2.waitKey(0)
