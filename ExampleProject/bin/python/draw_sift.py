import cv2
import numpy as np

def resize_image(img_path):
    # Load the image in grayscale
    img = cv2.imread(img_path, cv2.IMREAD_GRAYSCALE)
    
    # Get the current dimensions of the image
    height, width = img.shape
    
    # Determine the scaling factor
    max_dimension = max(height, width)
    scale = 640 / max_dimension
    
    # Calculate the new dimensions
    new_width = int(width * scale)
    new_height = int(height * scale)
    
    # Resize the image
    resized_img = cv2.resize(img, (new_width, new_height))
    
    return resized_img
    
def draw_matches(img1_path, img2_path, global_index):
    # Load the images in grayscale
    img1 = cv2.imread(img1_path, cv2.IMREAD_GRAYSCALE)
    img2 = cv2.imread(img2_path, cv2.IMREAD_GRAYSCALE)

    resized_img1 = resize_image(img1_path)
    resized_img2 = resize_image(img2_path)

    # Check if images loaded properly
    if resized_img1 is None or resized_img2 is None:
        print("Error loading images!")
        return

    # Initialize the SIFT detector
    sift = cv2.SIFT_create()

    # Detect keypoints and descriptors
    kp1, des1 = sift.detectAndCompute(resized_img1, None)
    kp2, des2 = sift.detectAndCompute(resized_img2, None)

    # Create BFMatcher object
    bf = cv2.BFMatcher()

    # Match descriptors using k-nearest neighbors
    matches = bf.knnMatch(des1, des2, k=2)

    # Apply ratio test to find good matches
    good = []
    for m, n in matches:
        if m.distance < 0.75 * n.distance:
            good.append(m)

    # Sort the good matches based on the distance - best matches first
    good = sorted(good, key=lambda x: x.distance)

    # Limit to the 1000 best matches
    if len(good) > 25:
        good = good[:25]
    
    # Extract location of good matches
    points1 = np.float32([kp1[m.queryIdx].pt for m in good])
    points2 = np.float32([kp2[m.trainIdx].pt for m in good])

    # Find homography using RANSAC
    H, mask = cv2.findHomography(points1, points2, cv2.RANSAC,100.0)

    # Number of inliers
    inliers = np.sum(mask)
    print("Number of inliers: ", inliers)
    print("Total good: " + str(len(good)))
    
    total_good = len(good)

    # Calculate the ratio
    if total_good > 0:  # Prevent division by zero
        ratio = inliers / total_good
    else:
        ratio = 0  # Handle case where total_good is zero

    print(f"Ratio of inliers/total good: {ratio:.4f}")  # Format ratio to 2 decimal places

    # Draw first 10 good matches.
    draw_params = dict(matchColor=(0, 255, 0),  # Draw matches in green color
                       singlePointColor=None,
                       matchesMask=mask.ravel().tolist(),  # Draw only inliers
                       flags=2)

    img_matches = cv2.drawMatches(resized_img1, kp1, resized_img2, kp2, good, None, flags=cv2.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS)

    # Save the image to a file using the global index to create a unique filename
    filename = f"sift_matched_{global_index}.png"
    cv2.imwrite(filename, img_matches)

# Lists of paths to the images
img1_paths = [ "1Hill.JPG", "2Hill.JPG", "S3.jpg", "b.jpg", "P1011370.JPG", "P1011069.JPG",
				"P1010372.JPG", "grail03.jpg", "DSC_0178.jpg", "bike1.png", "Yosemite1.jpg", "img2.png", "h1.jpg",
				"base1.jpg", "Test1025.jpg", "Test1027.jpg", "Test81.jpg", "Test1027.jpg", "Test1025.jpg", "Test81.jpg",
				"Test1025.jpg", "Test72.jpg", "Test65.jpg", "Test21.jpg", "Test1027.jpg", "Test1027.jpg",
				"Test1500.jpg", "Test1500.jpg", "Test81.jpg", "Test81.jpg", "Test3000_rot.jpg", "Test47_rot.jpg",
				"Test3030.jpg", "Test1031.jpg", "Test1027.jpg", "Test1025.jpg", "Test1024.jpg", "Test506.jpg",
				"Test506.jpg", "Test404.jpg", "Test705.jpg", "Test705.jpg", "Test766.jpg", "Test766.jpg", "Test82.jpg",
				"Test5000_rot.jpg", "Test3000_rot.jpg", "Test1500.jpg", "Test1310_rot.PNG", "Test1199_rot.PNG",
				"Test1000_rot.jpg", "Test2120_rot.jpg", "Test1999_rot.jpg", "Test4.jpg", "Test6.jpg", "Test21.jpg",
				"Test34_rot.jpg", "Test37.jpg", "Test47_rot.jpg", "Test48_rot.png", "Test65.jpg", "Test70.jpg",
				"Test99.jpg", "Test120_rot.jpeg", "Test121_rot.png", "Test122_rot.png", "Test123_rot.jpg",
				"Test200_rot.jpg", "Test211_rot.jpg", "Test240.jpg", "Test300.jpg", "Test400.jpg", "Test600.jpg",
				"Test800.jpg" ]
img2_paths = [ "2Hill.JPG", "3Hill.JPG", "S5.jpg", "c.jpg", "P1011371.JPG", "P1011070.JPG",
				"P1010373.JPG", "grail04.jpg", "DSC_0179.jpg", "bike2.png", "Yosemite2.jpg", "img3.png", "h2.jpg",
				"base2.jpg", "Test1026_4.jpg", "Test1028_3.jpg", "Test85.jpg", "Test1028_2.jpg", "Test1026_3.jpg",
				"Test82_2.jpg", "Test1026_2.jpg", "Test70.jpg", "Test67.jpg", "Test23.jpg", "Test1029.jpg",
				"Test1030.jpg", "Test1502.jpg", "Test1503.jpg", "Test83.jpg", "Test84.jpg", "Test3002_rot.jpg",
				"Test49_rot.jpg", "Test3031.jpg", "Test1032.jpg", "Test1028.jpg", "Test1026.jpg", "Test1023.jpg",
				"Test507.jpg", "Test508.jpg", "Test405.jpg", "Test706.jpg", "Test707.jpg", "Test767.jpg", "Test768.jpg",
				"Test81.jpg", "Test5001_rot.jpg", "Test3001_rot.jpg", "Test1501.jpg", "Test1311_rot.PNG",
				"Test1200_rot.PNG", "Test1001_rot.jpg", "Test2121_rot.jpg", "Test2000_rot.jpg", "Test5.jpg",
				"Test7.jpg", "Test22.jpg", "Test35_rot.jpg", "Test38.jpg", "Test48_rot.jpg", "Test49_rot.png",
				"Test66.jpg", "Test71.jpg", "Test100.jpg", "Test124_rot.jpeg", "Test125_rot.png", "Test126_rot.png",
				"Test127_rot.jpg", "Test201_rot.jpg", "Test212_rot.jpg", "Test241.jpg", "Test310.jpg", "Test410.jpg",
				"Test610.jpg", "Test810.jpg" ]

# Initialize a global index variable if not already done
global_index = 0

# Loop over the arrays
for img1_path, img2_path in zip(img1_paths, img2_paths):
    print(img1_path + " and " + img2_path)
    draw_matches("..\\..\\" + img1_path, "..\\..\\" + img2_path, global_index)
     # Increment the global index for future use
    global_index += 1