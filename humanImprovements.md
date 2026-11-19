## refactoring code
What improvements can you make to the design/architecture of your code?
- service.js and src/ need fixing, so that it cleaner
- Remove code that are commented and not needed, unused files/methods, and add summaries on each page to ensure everyone knows what that file does
How can you split up large methods or classes into smaller components?
- adding helper methods 
- The rest of our method is under 100 lines currently.
Are there unused files/methods that can be removed?
- We have results folder is currently empty, and clean up empty, redunddant one
- loose files scatter around
Where would additional Java interfaces be appropriate?
- ... Thinking about it for a while now, maybe implement or check csvResultWriter again
- on methods that are long and would need a lot of tests on
How can you make things simpler, more-usable, and easier to maintain?
- Updated README and right now, names really confusing and a lot to look at, will check and go through again to find the best way for easier look at. (the typo that i fix todayfor videoProcessor and videoprocessor shouldnt happend at all)
- helper methods
Other refactoring improvements?
- just adding comments 
## adding tests
- What portions of your code are untested / only lightly tested? TrackingResultTest has been lightly tested, and I feel like CentroidTrackerTest needs more tests
Where would be the highest priority places to add new tests?
- For TrackingResultTest and VideoProcessorTest because we should be checking if an mp4 file is too big, no color at all, or things like that
Other testing improvements?

 ## improving error handling
What parts of your code are brittle?
- implementation that is redundant 
Where could you better be using exceptions?
Where can you better add input validation to check invalid input?
How can you better be resolving/logging/surfacing errors? Hint: almost any place you're using "throws Exception" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that might be thrown or caught.
Other error handling improvements?

## writing documentation
What portions of your code are missing Javadoc/JSdoc for the methods/classes?
What documentation could be made clearer or improved?
Are there sections of dead code that are commented out?
Where would be the most important places to add documentation to make your code easier to read?

## Other documentation improvements?
improving performance (optional)
What parts of your code / tests run particularly slowly?
What speed improvements would most make running / maintaining your code better?
Other performance improvements?
hardening security (optional)
What packages / images are out of date / have security issues?
Where could you have better input validation in your code to prevent malicious use?
Other security improvements?
bug fixes (optional)
What bugs do you know exist?
What parts of the code do you think might be causing them?
Other bug fix improvements?
other
Any other improvements in general you could make?