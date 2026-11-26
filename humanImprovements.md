https://docs.google.com/document/d/1IlyKFAqe-aC9pzrdZfmNP48ut4Q_Jqeo9khovCuQX_Y/edit?usp=sharing

# Backend Repository

## Color Key (processor `src`)

- 🔴 **Interfaces**
- 🔵 **Records**
- 🟣 **Java Processes**

---

## Backend Repository

- Check the JavaDocs
- Refactor/add comments throughout Java code where necessary
- Remove any dead code, to keep code DRY
- We're gonna refactor the structure of our main folder as to not have it be so monolithic of a parent directory for our files

---

## Centroid-finder-backend

- `jobs`: contains the jobs stored with a UID
- `processor`
  - `sampleInput`
  - `sampleOutput`
  - `src`
    - `main` (Add new directories to separate interfaces, records and java processes.)
      - 🟣 `BinarizingImageGroupFinder.java`: Good
      - 🔴 `BinaryGroupFinder.java`: Interface dir
      - 🔴 `ColorDistanceFinder.java`: Interface dir
      - 🔵 `Coordinate.java`: Good (going to go inside Records dir)
      - 🟣 `DfsBinaryGroupFinder.java`: Needs to be moved to process dir
      - 🟣 `DistanceImageBinarizer.java`: Needs to be moved to process dir
      - 🟣 `EuclideanColorDistance.java`: process
      - 🟣 `Experiement.java`: process
      - 🟣 `FfmpegVideoFrameGrabber.java`
      - 🔵 `FrameData.java`: add to Records dir
      - 🔵 `Group.java`: add to Records dir
      - 🔴 `ImageBinarizer.java`: interface dir
      - 🔴 `ImageGroupFinder.java`: interface dir
      - 🟣 `ImageSummaryApp`
      - 🟣 `SamplingVideoGroupFinder.java`: process dir
      - 🔴 `VideoFrameGrabber.java`: Needs to be moved to the interface directory
      - 🔴 `VideoGroupFinder.java`: Needs to be moved in to the interface directory
      - 🔵 `VideoResult.java`: Needs to be moved to records dir
      - 🟣 `VideoResultWriter.java`: process dir
      - 🟣 `VideoSummaryApp.java`: process dir
    - `test`: Write more edge cases trying to break our current structure
  - `pom.xml`
- `server`
  - `lib`
    - `jobStore.js`: Add multi line comments for the methods
    - `validate.js`: Add multi line comments for the methods
  - `routes`
    - `process.js`: Add multi line comments for the methods
    - `thumbnail.js`: Add multi line comments for the methods
    - `videos.js`: Add multi line comments for the methods
  - `test`
    - `routes.test.js`: Complete, does not need changed
    - `validate.test.js`: Complete, does not need changed
  - `app.js`: Complete, does not need changed
  - `config.js`: Complete, does not need changed
  - `index.js`: We're going to change to `server.js` instead of `index.js`