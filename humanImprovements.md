- refactoring code
    - What improvements can you make to the design/architecture of your code?
        >1. potentially splitting the server/backend and front end into separate git repos
        >2. i could implement a best practice for code to adgere to like NASA does
    - How can you split up large methods or classes into smaller components?
        >1. i looked into all of my code and excluding the processor which was largely already made for us to fill in and the rest designed to not be touched, i feel like due to the react class and the backend focused and class before it, that the way i structured my server and react app are solid enough and any more abstracting might be potentially excessive.
    - Are there unused files/methods that can be removed?
        >1. there are a few potential "excess" .md files im not sure need to still be in the repo ( notes.md, options.md, plan.md). At the very least these might not need to be tracked by git. Or, maybe combine them into a single md with good headings.
        >2. the sampleInput and sampleOutput folders in the processor directory only exist for earlier assignment parts that directly referenced them and could be removed
        >3. there is an empty file 'frontend\src\hooks\useJobStatus.js' that can be removed
        >4. there is ghost code throught the server and processor directories that can be removed
        >5. there is a file `processor\src\test\java\VideoProcessorTest.java` that is unused and commented out entirely that can be removed.
    - Where would additional Java interfaces be appropriate?
        >1. i feel like the interfaces used in the processor were pretty solid, however when we were required to add video processing logic to the processor code, we did not add any interfaces that may have been useful. I dont know exactly how i d implement one, mayb something general that required a function that verified a valid video was added or threw an error.
    - How can you make things simpler, more-usable, and easier to maintain?
        >1. definitely could add more comments and function documention as well as line comments explain tricky parts of the function itself. this is something i wanted to do but wwas crunched for time most of this assignment.
        >2. maybe adda .md file that thoroughly explains how to install and use all parts of the project from the processor to the front end and server. that way if i were to open the project much later it be easier to get back into it or also if someone completely new to the project opened it.
    - Other refactoring improvements?
        >1. id like to add more documentation accross the entire project. 
- adding tests
    - What portions of your code are untested / only lightly tested?
        >1. Ive implemented most of the required tests and feel confident about the tests written for the processor, however the frontend and server could use more tests than the few ive implemented for them.
    - Where would be the highest priority places to add new tests?
        >1. more tests in the server, specifically testing that enpoints are reached and errors are handled gracefully.
    - Other testing improvements?
        >1. i can verify failures in tests, not just that the test work in an ideal scenerio
        >2. add more tests to frontend that cover more cases than the obvious ones ive used. maybe more obscure edge cases.
        >3. an imimediate test idea is to test that fgailed video processes dont print a csv for the failed job. similarly i can verify job id match the csv they create for consistency.
- improving error handling
    - What parts of your code are brittle?
        >1. i think the front end logic to handle processing jobs could potentially be prone to error if someone were to queue another job, or force the url to process a new job, or finally if someone tried to spoof a route and crashed the front end 
    - Where could you better be using exceptions?
        >1. in the video processor entry point i currently only check that there are 4 arguments passed into the video processor. I could definitely parse these 4 arguments (``` java -jar videoprocessor.jar, "<input>, <outputCsv>, <hexColor>, <threshold>```) and verify its correct data to use and not a spoof or incorrect characters that could crash the processor.
    - Where can you better add input validation to check invalid input?
        >1. in the video processor app entry. I could use similar techniques used in the imageSumary app since that was provided for us by the instructor, and i assume it has better input validation than my own used in the videoprocessorapp.
    - How can you better be resolving/logging/surfacing errors? Hint: almost any place - you're using "throwsException" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that -- might be thrown or caught.
    - Other error handling improvements?
        >1. i can verify that use effects clean themselves up and that.
- writing documentation
    - What portions of your code are missing Javadoc/JSdoc for the methods/classes?
        >1. i feel like the entire project and functions could use a a more genuine comment and documentation pass. currently my comments are either pretty good or just more like developer notes.
    - What documentation could be made clearer or improved?
        >1. all functions should have jdoc explinations and more technical logic should have vbrief comments explaining the algorithems or code design being implemented
    - Are there sections of dead code that are commented out?
        >1. absolutely. especially in the frontend and server and processor directories. I definitely need to comb through every file or have an agent isolate specific instances of large ghost code. Maybe that could be a test or tool for an agent?
    - Where would be the most important places to add documentation to make your code easier to read?
        >1. I would say the server because it lacks almost any Jdoc for anything. its pretty much just stray code.
    - Other documentation improvements?
        >1. a helpful and informative ReadMe entry or smoe external documentation file that explains a general usage and flow of the project when used correctly.

- improving performance (optional)
    - What parts of your code / tests run particularly slowly?
        >1. the processor currently does a lot of unnecessary logging that is only needed for development
    - What speed improvements would most make running / maintaining your code better?

    - Other performance improvements?
- hardening security (optional)
    - What packages / images are out of date / have security issues?
    - Where could you have better input validation in your code to prevent malicious use?
        >1. checking the arguments passed in for the imageProcessorApp as well as the videoProcessoApp
    - Other security improvements?
        >1. hardened cors requirements, although  i dont know how to do this and we currently only opperate from localhost so itd be overkill.
        >2. maybe some use of middleware to prevent usage of endpoints that requires a temporary key from local storage, though i dont know how useful this is
- bug fixes (optional)
    - What bugs do you know exist?
    - What parts of the code do you think might be causing them?
    - Other bug fix improvements?
- other
    - Any other improvements in general you could make?
        >1. definitely better documentation throught the project and deleting ghost code and unused files.
