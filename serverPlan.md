# Salamander API — Server Plan

## Pictures!

[View basic flow diagram of end to end process](https://docs.google.com/document/d/1rhOyxRyZ97zuouzpOMkT3qPv2V_SQ4UL3Uyw14H4xMw/edit?usp=sharing)

##  Basic Idea

The Express server will accept video-processing requests from the front end eventuallyand use existing Java centroid finder to process the videos. track processing jobs using a UUID and a JavaScript Map. return a thumbnail of the video and a CSV file with the centroid data when processing is complete.

### Implementation Steps

1. **Set up the Express server**
   - Configure Express and finish the provide endpoints. specified in the salamander API Readme
   - Make locally stored videos accessible to the server in a sort ofcollection

2. **Handle requests**
   - Validate the requested video exists in map and continue processing with centroid finder parameters.
   - Return an error if the video doesn't exist or the parameters are invalid.

3. **Prepare requests for processing**
   - Generate a UUID for each processing request.
   - Store the UUID in a Map with the processing state of request for polling, and CSV result path.


4. **Begin processing with centroid finder JAR**
   - Use Node.js `child_process` <-- (potentially something else to be learned, not locked in yet) to execute the centroid finder JAR file with the provided parameters.
   - begin polling the JAR process for completion or failure. do this by checking using a timeout to check the value in the Map for the UUID key. if the value is still "processing" then continue polling. if the value is "complete" or "failed" then stop polling and return the result to the client.

5. **Wait for Processor**
   - Detect when the Java process finishes or fails via a callback or event listener or polling function.
   - Update the corresponding UUID entry in our Map when processing completes.

6. **Provide processing results**
   - return completed CSV files accessible to the client Map via uuid key.
   - Implement thumbnail generation using FFmpeg.

## Testing and Validation
- Use postman currently because front end is out of scope of assignment.

## Questions to Consider

- what do i do i the jar fails? (ive not considered this deeply yet)
- what if videos are abnoral length? (i.e. 0 length or very long)
