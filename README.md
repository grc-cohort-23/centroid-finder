# centroid-finder

## *DO THIS FIRST* Wave 0: AI Rules 
AI is *NOT ALLOWED* for generating implementations of the classes.
AI is allowed for helping you make test cases.

Don't have it just create the tests mindlessly for you though! Make sure you're actively involved in making the tests.

DO NOT MIX HUMAN AND AI COMMITS.
EVERY COMMIT THAT USES AI MUST START WITH THE COMMIT MESSAGE "AI Used" AND IT MUST ONLY CREATE/ALTER TEST FILES

For this wave, please have each partner make a commit below with their username acknowledging that they understand the rules, according to the following format:

"I, KJeansGR, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

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
To validate your code is working, make sure you're in the centroid-finder directory and run the below command:

```
javac -cp lib/junit-platform-console-standalone-6.0.3.jar src/*.java && java -cp src ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

This will compile your files and run the main method in ImageSummaryApp against the sample image with a target color of orange and a threshold of 164. It should binarized.png and groups.csv which should match the corresponding files in the sampleOutput directory.

Once you have confirmed it is working, clean up your code, make sure it's committed and pushed, and make a PR to submit. Great job!

## Optional Wave 7: Enhancements?
If you want to, you can make a new branch to start experimenting. See if you can come up with a better color distance method (hint: look up perceptual color spaces). See if you can make your code more efficient or mor suited to spotting salamanders! Experiment with other test files. PLEASE MAKE SURE THIS IS IN A SEPARATE BRANCH FROM YOUR SUBMISSION.


# centroid-finder Part 2

## Overall Goal:
The overall goal of this project will be to make your centroid finder project be able to process mp4 videos. When working properly, it will generate a CSV that tracks the largest centroid over time. The first column will be the number of seconds since the beginning of the video, and the second and third columns will be the x and y coordinates of the largest found centroid at that time. If no centroid is found at that timestamp, coordinates of (-1, -1) should be used. In this portion of the project you will be taking each frame of video and feeding it to your centroid finder logic. You will create a command line utility that can process videos, for real!

You will be creating a jar that can run like so:

java -jar videoprocessor.jar inputPath outputCsv targetColor threshold
Everything after videoprocessor.jar is command line arguments that will be taken in.

## Wave 1: Planning
Talk with your partner about how you might accomplish the overall goal! THIS MUST BE DONE IN AN OBJECT ORIENTED WAY. YOU WILL NOT RECIEVE CREDIT IF ALL OF THE NEW LOGIC IS IN ONE BIG NEW CLASS. Really think about what the different components are! Look at centroid finder and consider looking back at Truffula, Screwtape, and Ramblebot on how you might architect your code.  Think about how the arguments are parsed, what existing classes you can RE-USE. Do not just do everything over again from scratch! Think about how to write good tests for the various components.

Make a file "plan.md" that details your plans. Write out a plan and draw a picture of what your architecture is going to look like. Please write this plan yourself. You can consult an AI for advice, but don't just copy-paste a plan it suggests. If you want to make your plan in Google Docs, please have plan.md include a link to your document and make sure the document is shared with the instructor.

Your plan doesn't need to be perfect! It will almost certainly change as you implement.

## Wave 2: Implementing & Testing
Start working on implementing! You can use an AI for help, but do not just copy-paste things without understanding them. I WILL BE ASKING YOU TO EXPLAIN YOUR CODE. Make tests as you work, and make many commits along the way! Feel free to adjust your plan as needed, and even switch to a different video processing library if you decide that the one you originally chose is not a good fit.

## Wave 3: Packaging
Use Maven to create an executable JAR! Hint: you may consider using the Maven Assembly PluginLinks to an external site.. Please make sure not to commit your JAR! 

## Wave 4: Validating
See if your code works on a real salamander videoLinks to an external site.. Discuss with your partner how you can validate that it's actually tracking the mander! Think also about how you can choose a good color and threshold for it.  Add to the end of your plan.md how you tested it. DO NOT COMMIT THE VIDEO TO YOUR REPO! Modify your .gitignore to help prevent this mistake.