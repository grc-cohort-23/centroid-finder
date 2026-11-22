# Refactoring code
- What improvements can you make to the design/architecture of your code?
    The code is organized in folders and files
    There are a few csv outside of the code 
- How can you split up large methods or classes into smaller components?
    The java methods are in main -> java -> centroidfinder
    Typescript functions are stored in server folder

- Are there unused files/methods that can be removed?
    Could not find any unused files 

- Where would additional Java interfaces be appropriate?
    

- How can you make things simpler, more-usable, and easier to maintain?
- Other refactoring improvements?
    move tests to a separate folder(for example called tests) for consistency with Java in the server/src folder... its organized that way in the processor folder

# Adding tests
- What portions of your code are untested / only lightly tested? Where would be the highest priority places to add new tests?
- Other testing improvements?
     neonJobStore only has a few tests.

# Improving error handling
- What parts of your code are brittle?
    The typscript is a bit brittle 
    Dropped database connection can crash the whole server.
    The pg Pool has no error listener, so a dropped Neon connection can crash the server

    a bad threshold gives 500 instead of 400.

- Where could you better be using exceptions?
    CsvOutputwriter , imageSummaryApp

- Where can you better add input validation to check invalid input?
    index.ts, imageSummaryapp

- How can you better be resolving/logging/surfacing errors? Hint: almost any place you're using "throws Exception" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that might be thrown or caught.

    imageSummaryApp has catch(exception e) that would need to be replaced

Other error handling improvements?

# Writing documentation
What portions of your code are missing Javadoc/JSdoc for the methods/classes?
What documentation could be made clearer or improved?
Are there sections of dead code that are commented out?
Where would be the most important places to add documentation to make your code easier to read?
Other documentation improvements?
    
    We dont have anu JSdocs for the methods and classes. We have comments only.
    ImageCentroidTracker, DfsBinaryGroupFinder, EuclideanColorDistance do not have javadocs 
    ReadMe server needs to be updated.
    Did not find any commented out code.
    Update read me and add jsdoc to each method/function/class

# Improving performance (optional)
What parts of your code / tests run particularly slowly?
What speed improvements would most make running / maintaining your code better?
Other performance improvements?

# Hardening security (optional)
What packages / images are out of date / have security issues?
Where could you have better input validation in your code to prevent malicious use?
Other security improvements?

# Bug fixes (optional)
What bugs do you know exist?
What parts of the code do you think might be causing them?
Other bug fix improvements?

# Other
Any other improvements in general you could make?

-One open issue: the plan notes at serverPlan.md:130 that job records are shared through Neon, but each result CSV is saved only on the laptop that ran the JAR. A teammate could see your job marked "done" but not have the file on their machine.

-having a centroid that could possibly track heat signatures in camera systems? Or even having it be able to read live feed..