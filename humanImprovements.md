# Code Refactoring 
- As of now most of our functions in our components has lots of code, 
we try to reduce code in some of our functions. (Workspace.jsx and App.jsx)
- We can also try to reuse function, functionality that can be re used by importing.
- There are also test files that can be re-organized into the test folder.

# Adding Test 
- Need to include tests for files that have no tests.
# Improve error handling
- As of now we have common cases for when a error occurs, we need to have more robust error handling for. invalid videos and other error cases (jobs.js, runner.js).
- Need to include spcific speical cases to throw IO exceptions and other errors.
# Documentation
- Some classes and methods in main needs java doc/explination.
- Front-end side also needs documentation on some of its files.
# Improving performance
improve the installation process so that anyone forking and clone as the able to run this without having to install new package


# Hardening security 
- Delete .env from our github repo.
# Bug fixes 
- Need to hanlde a bug where the app still receives data even when video is not playing.
