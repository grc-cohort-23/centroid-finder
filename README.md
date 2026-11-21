# centroid-finder

## *DO THIS FIRST* Wave 0: AI Rules 
AI is *NOT ALLOWED* for generating implementations of the classes.
AI is allowed for helping you make test cases..

Don't have it just create the tests mindlessly for you though! Make sure you're actively involved in making the tests.

DO NOT MIX HUMAN AND AI COMMITS.
EVERY COMMIT THAT USES AI MUST START WITH THE COMMIT MESSAGE "AI Used" AND IT MUST ONLY CREATE/ALTER TEST FILES

For this wave, please have each partner make a commit below with their username acknowledging that they understand the rules, according to the following format:

"I, YOUR_GITHUB_USERNAME, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

## Wave 1: Understand
Read through ImageSummaryApp in detail with your partner. Understand what each part does. This will involve looking through and reading ALL of the other classes records and interfaces. This will take a long time, but it is worth it! Do not skimp on this part, you will regret it! Also look at the sampleInput and sampleOutput folders to understand what comes in and what goes out.

As you read through the files, take notes in notes.md to help you and your partner understand. Make frequent commits to your notes.

## Wave 2: Implement DfsBinaryGroupFinder
This class takes in a binary image array and finds the connected groups. It will look very similar in many ways to the explorer problem you did for DFS! You'll need to understand the Group record to do this well.

Consider STARTING with the unit tests. Remember, you can use AI to help with the unit tests but NOT the implementation. Any AI commit must start with the message "AI Used"

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.

## Wave 3: Implement EuclideanColorDistance
Implement EuclideanColorDistance. You may consider adding a helper method for converting a hex int into R, G, and B components.

Again, consider starting with unit tests. You may consider using WolframAlpha to help you get correct expected values.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.

## Wave 4: Implement DistanceImageBinarizer
To do this you will need to research `java.awt.image.BufferedImage`. In particular, make sure to understand `getRGB` and `setRGB`. When creating a new image, you can use the below to start the instance:

```
new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
```

Note that a lot of this class will be calling methods in BinaryGroupFinder and ColorDistanceFinder!

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here.

HINT: `getRGB` returns a 32-bit AARRGGBB color (includes alpha channel). However, ColorDistanceFinder expects the colors to come in RRGGBB format (no alpha channel (most significant 8 bits set to 0)). What can you do to make this conversion happen?

## Wave 5: Implement BinarizingImageGroupFinder
This implementation will be relatively short! It will mostly be calling methods in ImageBinarizer and BinaryGroupFinder.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here. I recommend NOT using any external library other than JUnit. If the AI wants to use another external library, consider asking it not to and to make stubs instead.

## Wave 6: Validation
To validate your code is working, make sure you're in the `processor` directory (`cd processor` from the centroid-finder directory) and run the below command:

```
mvn compile
java -cp target/classes io.github.nadia5129.centroidfinder.ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

(`mvn exec:java` now runs the video processor, so the image app is run directly with `java`.)

To run the unit tests:

```
mvn test
```

This will compile your files and run the main method in ImageSummaryApp against the sample image with a target color of orange and a threshold of 164. It should binarized.png and groups.csv which should match the corresponding files in the sampleOutput directory.

Once you have confirmed it is working, clean up your code, make sure it's committed and pushed, and make a PR to submit. Great job!

## Optional Wave 7: Enhancements?
If you want to, you can make a new branch to start experimenting. See if you can come up with a better color distance method (hint: look up perceptual color spaces). See if you can make your code more efficient or mor suited to spotting salamanders! Experiment with other test files. PLEASE MAKE SURE THIS IS IN A SEPARATE BRANCH FROM YOUR SUBMISSION.

## Video Processing
video 
This branch will be used to add video processing capabilities to our centroid finder project.

The video processor reads an MP4 one frame at a time and writes a CSV that tracks the largest centroid of the target color over time. Each row is `seconds,x,y`. If no centroid is found in a frame, the row is `seconds,-1,-1`. See `plan.md` for the design.

All of the commands below are run from the `processor` folder:
```
cd processor
```

### Build the JAR
```
mvn package
```
This runs all the tests, then builds `target/videoprocessor.jar` (the JAR is large because it includes FFmpeg for every operating system, and it is not committed).

### Run it
```
java -jar target/videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>
```
Example with the sample balloon video, tracking the red-orange balloon:
```
java -jar target/videoprocessor.jar sampleInput/bright_balloon_sample_video.mp4 output.csv FF4500 100
```

Or run it with Maven without building the JAR:
```
mvn compile exec:java -Dexec.args="sampleInput/bright_balloon_sample_video.mp4 output.csv FF4500 100"
```

### Arguments
- `inputPath` - the video file to read
- `outputCsv` - where to write the CSV
- `targetColor` - the color to track, as 6 hex digits in RRGGBB format (example: `FF4500`)
- `threshold` - how close a pixel's color must be to the target color to count, as a whole number 0 or higher

## Server
server
This branch adds a server that takes requests to process videos and makes the results available. It implements the Salamander API by calling `videoprocessor.jar`; all of the centroid finding stays in the Java code. See `serverPlan.md` for the plan.

The server uses Express with TypeScript.

### Project layout
```
centroid-finder/
├── .env            paths the server needs (like the videos folder and the JAR)
├── plan.md         design of the video processor
├── serverPlan.md   design of the server
├── processor/      Maven Java project (the video processor)
└── server/         Express + TypeScript server
```

### Set up the server
After cloning or pulling, install the server's packages once (`node_modules/` is not committed):
```
cd server
npm install
```
