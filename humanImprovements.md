# Human Improvements!
### What do we want to do?
#Neat_things #we #could_do
###### I know, *how* about _improving optimization_ for the user!

Note, we are completing this file `without AI`, finding issues `without AI`, and writing this file `without AI`.

---
### Refactoring code
What improvements can you make to the design/architecture of your code?
`For the loading of our centroid image, we could buffer with the last image displayed rather than displaying a loading screen, requiring remaking of our status system or creating previous image holders. That or we could access detailed information on our images through different routes utilizing a new style.`
How can you split up large methods or classes into smaller components?
`It would actively hinder us if we further split our methods into smaller ones, our logic is distributed evenly in our components`
Are there unused files/methods that can be removed?
`The previous mds as we are not actively using them in our design process`
Where would additional Java interfaces be appropriate?
`A dedicated single image binarizer, this could be done in javascript though`
How can you make things simpler, more-usable, and easier to maintain?
`Designing the main page in a more intuitive/pleasant fashion `
Other refactoring improvements?
`Mostly page design improvements, addressing any concerns with tests so future development wouldn't mess something integral up`
>
---
### Adding tests
What portions of your code are untested / only lightly tested?
`The Server, as it is difficult to process long videos constantly and efficiently`
Where would be the highest priority places to add new tests?
`The User Upload section and Server section, make sure that any odd videos formats will work as intended`
Other testing improvements?
`Finding more usable files to test with`
>
---
### Improving error handling
What parts of your code are brittle?
`The Processing Job section, file upload`
Where could you better be using exceptions?
`In file upload, server exceptions as a close second`
Where can you better add input validation to check invalid input?
`In GET: requests and POST: requests we need exceptions for too high an amount of jobs on the server end, it shouldn't be up to the Clients code`
How can you better be resolving/logging/surfacing errors? Hint: almost any place you're using "throws Exception" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that might be thrown or caught.
`Our frontend code is already really spiffy on displaying to the user, however more developer-directed messages could be implemented, maybe some logs system.out.println-ed to the server command line? It would be good to confirm proper reception of data and update on its completion`
Other error handling improvements?
> 
---
### Writing documentation
What portions of your code are missing Javadoc/JSdoc for the methods/classes?
`Our frontend api, our server functions`
`For the processor: CsvReportWriter, JCodedVideoFrameReader, VideoCentroidTracker`
What documentation could be made clearer or improved?
`Interacting with the server, perhaps a usage.md with commonly used routes, and what server responses to save to check your job later`
Are there sections of dead code that are commented out?
`Thankfully, no. No sections of dead code commented out here`
Where would be the most important places to add documentation to make your code easier to read?
`The Server regarding interfacing requests and such`
Other documentation improvements?
>
---
>### Improving performance (optional)
>What parts of your code / tests run particularly slowly?
>High Resolution video processing on the server
>What speed improvements would most make running / maintaining your code better?
>Optimization features, such as quality crunching or resolution lowering before processing
>Other performance improvements?
>
---
>### Hardening security (optional)
>What packages / images are out of date / have security issues?
>
>Where could you have better input validation in your code to prevent malicious use?
>
>Other security improvements?
>
---
>### Bug fixes (optional)
>What bugs do you know exist?
>
>What parts of the code do you think might be causing them?
>>
>Other bug fix improvements?
>>
---
### Other
Any other improvements in general you could make?
>

---