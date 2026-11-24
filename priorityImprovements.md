## refactoring code (**required**)
- Validate all server and CLI parameters.

- Make Java failures produce non-zero process exits. (robot improvements)

## adding tests (**required**)
- after i refactor a lot of the server logic and front end logic to validate parameters i wiwll need to add tests that verify that these checks are working correctly 

- adding more tests for frontend code specifically edge cases and in general more tests that test the website instead of just the video and image processor logic because the only test file literlly is named "algorithems.test" which indicates it doesnt test much else about the front end
  
## improving error handling (**required**)
- error handling can be added for parameter/argument validation in the front end also specifically error handling for invalid video formats or abnormal video lengths

- error handling for if multiple video processes are attemted, if a uplicate process is started, in general any complications with video processing that effect an already ongoing process

## writing documentation (**required**)
- Update the README to reflect the entire project and usage in a way that anyone with zero knowledge of the project can understand.
- adding more robust and understandabe Jdoc comments for all functions and files 

## improving performance (**optional**)
- remove unnecessary console logs in the processor that may slow down funtionality
- limit processing requests 
- limit video file size for processing

## hardening security (**optional**)
- verify cors is working even only just with Localhost
- potentially tie the "download csv" or polling to a local storage id/cookie

## bug fixes (**optional**)
- i am not aware of any bugs 
- that was a joke

## other (**optional**)
- removing ghost code and unused scripts. There are a few entirely unused scripts " <em>frontend\src\hooks\useJobStatus.js, processor\src\test\java\VideoProcessorTest.java</em> " and a bunch of ghost code throught all 3 core directories (processor, frontend, server)
- stress test by doing random unnatural stuff an imbecile would do.
