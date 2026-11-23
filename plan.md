How will we accomplish processing mp4 videos with our per-pixel image centroid finder?

Example: To process mp4 videos into our image-foundation centroid finder we will ____ in order to make it compatible with our current architecture.

Without making one new class, without doing everything over again from scratch, we will ___ to accomplish a video parsing version of our image parsing application. Our solution is layered on top of the image processing component by slicing the video into individual images, feeding back a list of csv's and binarized images according to the video



        <<  THE PLAN    >>

Summary: Using JCodec we translate
the input video into a series of images,
each slice gives a csv file that gives
us our desired data.

Outcomes:
1: Iterable list of csv's
2: Binarized video stream output
3: Clean and easy feature implementation


Step 1:
Load up CentroidFinder as is.

Step 2:
Open JCodec example code and documentation,
iterate over a list of video frames as images.
Every image runs our current CentroidFinder
to scrape binarized.png and groups.csv files.

Step 3:
Put it together, through folders or file identifiers
make an easily iterable output order that makes our
generated data accessible.

Step 4:
Create tests to validate our program.

Step 5:
Submit!