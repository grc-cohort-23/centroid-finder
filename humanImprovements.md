# Centroid Finder Improvements

# Refactoring Code
- ImageSummaryApp: I think this class could be organized better. Some of the work it does could be moved into smaller methods so the code is easier to read and understand.

- We could organize videos and thumbnail code into into separate files to make it easier to work with and easier to go back to.

# Adding Tests
- In DfsBinaryGroupFinder we could add more tests for finding connected groups. For example we can have test images with no groups multiple groups and larger groups to make sure everything works correctly.

- We could add more tests for what happens when a video is missing or when creating a thumbnail fails. This would help us catch problems before they happen.

- Adding testing for our frontend.

# Improving Error Handling

- We could be more specific about which errors we catch so it is easier to figure out what went wrong.

- Image loading: We should check if an image loaded correctly before trying to process it. This would help avoid errors when someone provides an invalid image.

- Filename checking: We could improve how the server checks filenames before opening videos. This would help prevent problems with invalid file paths that we were having issues with. 

# 4. Writing Documentation
- Add comments to some classes and methods could use better explanations of what they do.  

# Other Improvements

- Improve on CSS


 