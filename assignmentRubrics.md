# centroid-finder Part 1

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

<hr>

# centroid-finder Part 3

## Overall Goal:
The overall goal of this project will be to make an express webserver that can handle requests to have videos processed and make the results available. It will need to implement this Salamander APILinks to an external site.. YOU WILL NOT BE MAKING A FRONTEND AS PART OF THIS PROJECT. You will handle the creation of the front end in your React class.

## Wave 1: Setup
Create a new branch named "server". Make sure to make the new branch from your video branch! All of your video commits should be in the server branch.
Create new empty files ".env" and "serverPlan.md". They can be empty for now.
Reorganize your files by creating new folders "server" and "processor". Move all of the Java-specific logic into "processor". It will roughly (not exactly) look like the below.
centroid-finder/
```text
├── .github
├── .gitignore
├── LICENSE
├── README.md
├── plan.md
├── serverPlan.md
├── .env                  # Environment variable file (e.g., for JAR path)
├── server/               # Express server
│   ├── package.json
│   ├── index.js
│   └── ...other files
├── processor/            # Maven Java project
    ├── pom.xml
    ├── sampleInput
    ├── sampleOutput
    └── src/
        └── main/
            └── java/
                └── ...
```

Change to the server directory and use npm init to start a new node project.
Install Express.js in your new node project.
Commit and push everything!

## Wave 2: Planning
Look through the Salamander APILinks to an external site. with your partner. In serverPlan.md write notes about what you understand and any questions you have. Then, talk with your partner about how you might implement this server in Express. Look through the hints below for assistance.

Note that for now there is no way to upload videos. They will be stored directly on your computer in a folder that express can access.

In your serverPlan.md file, write out a plan and draw a picture of what your architecture is going to look like. Please write this plan yourself. You can consult an AI for advice, but don't just copy-paste a plan it suggests. If you want to make your plan in Google Docs, please have plan.md include a link to your document and make sure the document is shared with [instructor].email

YOUR PLAN MUST INVOLVE YOUR SERVER CALLING YOUR JAR!!!! DO NOT HAVE ANY GRAPH SEARCH LOGIC IN YOUR EXPRESS CODE.

Your plan doesn't need to be perfect! It will almost certainly change as you implement.

**Hints:**
```
Below are a few hints. These hints are not detailed. Instead, they give you overall directions of things to research!

You should store paths to things like the videos directory and the videoProcessor jar in your .env file. Then, use dotenv in node so your express server can find everything it needs to find
You can use UUID for making jobIds.
You can use ffmpeg for fetching the first frame of a video. Alternatively, you can choose to add to your videoProcessor JAR so it can have an option to return a frame instead of processing the video.
You can use child_process for creating a new process that can run the Java jar in the background. Do not have your request wait for the JAR processing to be finished to return a response! Make sure to use detached mode, ignore std.io, and unreference the child process. Use research and AI to help you understand what these things mean and how to do them in node!
You can use express.static to publicly host things like the video directory
You can choose to keep track of state either with a database or directly on the filesystem. Talk through with your partner the pros and cons of each!
```

## Wave 3: Implementing & Testing
Start working on implementing! You can use an AI for help, but do not just copy-paste things without understanding them. I WILL BE ASKING YOU TO EXPLAIN YOUR CODE. Make tests as you work, and make many commits along the way! Do not wait for your frontend to be ready to do testing. Do both unit testing, and use tools like PostmanLinks to an external site. for validating requests and inspecting responses. However, as you make progress on your frontend, you will want to start doing integration testing as well.

<hr>

# centroid-finder Part 4

## Overall Goal:
In this part of the project you will get a chance to improve your existing code. You will NOT be adding any new features for this phase of the project. Instead, you will be refactoring code, fixing bugs, adding tests, improving performance, improving error handling, hardening security, and writing documentation.

You will also practice more sophisticated use of AI, git branches, pull requests, and merge conflict resolution.

### Wave 1: Setup
Begin by merging your image branch into your main branch. This will mean that your main branch will have all of the newest updates to your code. However, please do not delete your other branches yet; I will be still looking at them to grade other parts of your project.

### Wave 2: Human Improvements - DO THIS WITHOUT AI
In this wave you will work with your partner to identify areas your code can be improved.

Make a file called humanImprovements.md. You will complete this file WITHOUT AI. Find the issues WITHOUT AI and write the file WITHOUT AI.

Work with your partner to review your code. Actually go back and look at all your files again! Take a look at your server code/tests, java code/tests, package.json, and pom.xml. Identify ways your code could be improved. As you go through, answer the below questions. You do not need to write full sentences, bullet points are fine. Again, do this WITHOUT AI. Make commits as you go. Spend a good deal of time on this!

- refactoring code
    - What improvements can you make to the design/architecture of your code?
    - How can you split up large methods or classes into smaller components?
    - Are there unused files/methods that can be removed?
    - Where would additional Java interfaces be appropriate?
    - How can you make things simpler, more-usable, and easier to maintain?
    - Other refactoring improvements?
- adding tests
    - What portions of your code are untested / only lightly tested?
    - Where would be the highest priority places to add new tests?
    - Other testing improvements?
- improving error handling
    - What parts of your code are brittle?
    - Where could you better be using exceptions?
    - Where can you better add input validation to check invalid input?
    - How can you better be resolving/logging/surfacing errors? Hint: almost any place - you're using "throwsException" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that -- might be thrown or caught.
    - Other error handling improvements?
- writing documentation
    - What portions of your code are missing Javadoc/JSdoc for the methods/classes?
    - What documentation could be made clearer or improved?
    - Are there sections of dead code that are commented out?
    - Where would be the most important places to add documentation to make your code easier to read?
    - Other documentation improvements?
- improving performance (optional)
    - What parts of your code / tests run particularly slowly?
    - What speed improvements would most make running / maintaining your code better?
    - Other performance improvements?
- hardening security (optional)
    - What packages / images are out of date / have security issues?
    - Where could you have better input validation in your code to prevent malicious use?
    - Other security improvements?
- bug fixes (optional)
    - What bugs do you know exist?
    - What parts of the code do you think might be causing them?
    - Other bug fix improvements?
- other
    - Any other improvements in general you could make?

### Wave 3: AI Improvements
In this wave you will work with AI to help you identify improvements. BE SKEPTICAL OF WHAT IT SUGGESTS! Think for yourself, do not just accept everything it says!!!

Make a file robotImprovements.md . Then follow the below steps to use GitHub Copilot to give you suggestions.

Open the command palette in VS code (ctrl+shift+p) or (cmd+shift+p).
Type Chat: Open Chat (Ask) and hit enter
This should open a Copilot chat. Make sure you've connected your GitHub account to your VS Code. A default GitHub account gets 50 free chat messages per month, a student developer pack account would get you more.
In the chat window, use the paper clip to attach the relevant folders and files. Include server code/tests, java code/tests, package.json, and pom.xml. DO NOT INCLUDE COMPILED FILES OR EXTERNAL PACKAGES LIKE node_modules
In the chat, send this message: Please look through this code and identify improvements I could make. Focus on things like refactoring code, fixing bugs, adding tests, improving performance, improving error handling, hardening security, and writing documentation. Please do not write the exact code fixes. Instead give me overall direction on what I should be focusing on.
Consider the response that comes back. You can chat with the AI to get further clarifications (though do remember the chat limit per month).
Be skeptical! It may give you suggestions that are outdated, have poor coding/security practices, that would make your API incompatible with the front end, or that are far too large / ambitious in scope.
Copy-paste the suggestions you consider the best into robotImprovements.md

### Wave 4: Prioritization
Make a file, priorityImprovements.md. In it, place the top two improvements that you and your partner think would be most important in each of the below categories. For items marked optional, it is OK if you did not identify any improvements. You can copy-paste directly from the human / robot improvement files.

refactoring code (required)
adding tests (required)
improving error handling (required)
writing documentation (required)
improving performance (optional)
hardening security (optional)
bug fixes (optional)
other (optional)
Wave 5: Improving
You must make at least 6 improvements. THERE MUST BE AT LEAST ONE IMPROVEMENT FROM EACH REQUIRED CATEGORY.

For each improvement:

MAKE A NEW BRANCH. Name the branch appropriately based off the improvement you're making.
Make the improvement. If you are refactoring, I STRONGLY encourage you to add additional tests before making changes so that you will be able to catch any new bugs you introduce. As always, make and push frequent commits (to your branch) as you go.
Test the improvement using your tests, manual commands, Postman, etc.
Make a Pull Request from your branch to main branch of your repository. In the title of your PR, please include the category of this improvement. For example "Refactoring: extracted new ArgumentParser class from the main method of VideoProcessor"
Have your partner review the pull request. They should look through your changes, and make comments on GitHub to suggest how to improve your code.
Make appropriate changes based off your partner's feedback and push them to your branch. They will automatically show up in the PR.
Once your partner is satisfied with your changes, they should comment LGTM (Looks good to me). The partner who made the PR and the partner who approves it must be different. You cannot approve your own PR!
Merge the PR into main.
Make sure to update any other active improvement branches you and your partner are working on by merging from main into your improvement branch. There will likely be merge conflicts to resolve, especially when you are refactoring! 
